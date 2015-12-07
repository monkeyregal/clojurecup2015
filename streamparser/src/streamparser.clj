(ns streamparser
  (:require [environ.core :refer [env]]
            [me.raynes.conch :as c]
            [me.raynes.conch.low-level :as sh]
            [clojure.core.async :refer [chan go go-loop <! <!! >! >!! sliding-buffer timeout thread]]
            [clojure.tools.logging :as log]
            [server]
            [spotify])
  (:import  [java.util Iterator])
  (:import  [java.nio ByteBuffer])
  (:import  [com.gracenote.gnsdk
             GnManager
             GnLicenseInputMode
             GnLocale
             GnLocaleGroup
             GnRegion
             GnDescriptor
             GnUser
             IGnUserStore
             GnLookupData
             GnLookupMode
             GnString
             IGnAudioSource
             IGnMusicIdStreamEvents
             IGnCancellable
             GnResponseAlbums
             GnMusicIdStreamIdentifyingStatus
             GnMusicIdStreamProcessingStatus
             GnStatus
             GnMusicIdStream
             GnMusicIdStreamPreset
             GnAlbumIterator
             GnMusicIdStreamIdentifyingStatus
             GnStorageSqlite
             GnLookupLocal
             GnLookupLocalStream])
  (:gen-class))

;; TODO
;; Store results
;; Use GnLookupData to retrieve more data
;; Add cache
;; Loop over stream

(def VERSION "0.0.1.0")

(def client-id (env :gnsdk-client-id))
(def client-tag (env :gnsdk-client-tag))
(def client-license (env :gnsdk-client-license))
(def gnsdk-lib (env :gnsdk-lib))

(def seven-seconds-chan (chan (sliding-buffer 1000)))
(def song-channel (chan (sliding-buffer 1000)))

(defn ffmpeg-stream [url]
  (log/info "start-stream" url)
  (assoc
   (sh/proc "ffmpeg" "-i" url "-f" "wav" "-ar" "44100" "-shortest" "pipe:")
   :url url))

(defn push-onto-stream [c logger ffmpeg-process]
  (thread (loop []
            (let [data-size 10584000 ;; 60 seconds of 44100 16-bit stereo
                  from (:out ffmpeg-process)
                  bytes (byte-array data-size)]
              (loop [total 0]
                (let [read (.read from bytes total (- data-size total))
                      new-total (+ total read)]
                  (if (>= new-total data-size)
                    (>!! c {:bytes bytes
                            :read  (min data-size new-total)
                            :url   (:url ffmpeg-process)
                            :logger logger})
                    (if (> read 0)
                      (recur new-total)
                      (do (sh/destroy ffmpeg-process)
                          (log/info "stream-closed" (:url ffmpeg-process))))))))
            (log/debug "delivered 60 seconds for: " (:url ffmpeg-process))
            (when (< 0 (.available (:out ffmpeg-process)))
              (recur)))))

(def last-songs (atom {:3fm nil
                       :sky nil}))

(defn emit-song?! [msg]
  (let [lookup (:stream-id msg)
        last   (lookup @last-songs)
        song   (select-keys msg [:artist :track])]
    (log/debug "Emit?" song)
    (if (not= song last)
      (do (log/debug "Emit!" song)
          (swap! last-songs (fn [prev] (assoc prev lookup song))))
      (log/debug "Skip" song))))

(def playlist {:3fm "0Dk9ouTKwXzGpqkjVavDRl"
               :sky "23xdXOfVgdQlDDc895COGM"
               :all "0zENxOQG6nXtN0ZYfud7FW"})

(def owner "monkeyregal")

(defn handle-song-channel [c]
  (thread
    (loop []
      (let [msg (<!! c)
            song (select-keys msg [:stream-id :artist :track])]
        (when (emit-song?! msg)
          (log/info "Handling" song)
          (try
            (log/debug "last 100" song)
            (swap! server/last-100-songs (fn [prev] (-> prev (conj msg) (#(take 100 %)))))

            (log/debug "add stream song" song)
            (spotify/add-song owner (playlist (:stream-id msg)) (:artist msg) (:track msg))

            (log/debug "trim stream" (:stream-id song))
            (spotify/trim-playlist owner (playlist (:stream-id msg)) 250)

            (log/debug "add all song" song)
            (spotify/add-song owner (playlist :all) (:artist msg) (:track msg))

            (log/debug "trim all")
            (spotify/trim-playlist owner (playlist :all) 250)
            (catch Exception e (log/error e))))
        (recur)))))

(defn log-stream-error [c]
  (thread (loop []
            (let [msg (<!! c)]
              (log/error "STREAM:" (:url msg) " MSG:" msg)
              (recur)))))

(deftype AudioSource [ffmpeg-process]
  IGnAudioSource
  (sourceInit [_] 0)
  (sourceClose [_])
  (samplesPerSecond [_] 44100)
  (sampleSizeInBits [_] 16)
  (numberOfChannels [_] 2)
  (getData [_ data-buffer data-size]
    (let [from (:out ffmpeg-process)
          bytes (byte-array data-size)
          read (.read from bytes 0 data-size)]
      (when (< 0 read)
        (.put data-buffer bytes 0 read))
      (max read 0))))

(defn gn-iterator->iterator-seq [albums]
  (iterator-seq
   (reify java.util.Iterator
     (hasNext [_]
       (.hasNext albums))
     (next [_]
       (.next albums))
     (remove [_]))))

(defn ext-id->map [ext-id]
  {:source  (.source ext-id)
   :type    (.type   ext-id)
   :value   (.value  ext-id)})

(defn convert-result [album]
  (let [artist-display (.. album (artist) (name) (display))
        track-matched  (.. album (trackMatched))
        track-display  (.. track-matched (title) (display))
        track-duration (.. track-matched (duration))
        match-position (.. track-matched (matchPosition))
        match-duration (.. track-matched (matchDuration))
        ext-ids        (-> (.. track-matched (externalIds) (getIterator))
                           (gn-iterator->iterator-seq))]
    {:artist artist-display
     :track track-display
     :track-duration track-duration
     :match-position match-position
     :match-duration match-duration
     :ext-ids (into [] (map ext-id->map ext-ids))}))

(def next-timeout (atom 7000))

(defn handle-result [result stream-id]
  (let [albums (-> (.. result (albums) (getIterator))
                   (gn-iterator->iterator-seq))
        result (-> (map convert-result albums)
                   doall
                   first)
        result (if result (assoc result :stream-id stream-id) result)]
    (log/debug "Handling" result "for" stream-id)
    (when result
      (>!! song-channel result))))

(defn make-logger [stream-id] (reify
                       IGnMusicIdStreamEvents
                       (musicIdStreamProcessingStatusEvent [_ status _]
                         ;; (println status)
                         )
                       (musicIdStreamIdentifyingStatusEvent [_ status c]
                         (log/debug "status event:\t" status)
                         (if (= status GnMusicIdStreamIdentifyingStatus/kStatusIdentifyingEnded)
                           (.setCancel c true)))
                       (musicIdStreamAlbumResult [_ result _]
                         (handle-result result stream-id))
                       (musicIdStreamIdentifyCompletedWithError [_ error]
                         (log/error (.errorAPI error) "\t" (.errorCode error) "\t" (.errorDescription error)))
                       (statusEvent [_ status percent _ _ _]
                            ;; (println percent)
                         )))

(defn stream-music1 [c user]
  (thread (try (loop []
                 (let [msg (<!! c)
                       logger (:logger msg)
                       mids (GnMusicIdStream. user GnMusicIdStreamPreset/kPresetRadio logger)]
                   (.. mids (options) (resultSingle true))
                   (.. mids (options) (lookupData GnLookupData/kLookupDataExternalIds true))
                   (.. mids (options) (lookupData GnLookupData/kLookupDataGlobalIds true))
                   (doto mids
                     (.automaticIdentifcation false))
                   (.audioProcessStart mids 44100 16 2)
                   (.audioProcess mids (:bytes msg) (:read msg))
                   (.identifyAlbum mids)
                   (.identifyCancel mids))
                   (recur))
               (catch Exception e e))))

(defn stream-music [uri user]
  (let [mids (GnMusicIdStream. user GnMusicIdStreamPreset/kPresetRadio (make-logger "N/A"))]
    (.. mids (options) (resultSingle true))
    (.. mids (options) (lookupData GnLookupData/kLookupDataExternalIds true))
    (.. mids (options) (lookupData GnLookupData/kLookupDataGlobalIds true))
    (doto mids
      (.automaticIdentifcation false))
    (go-loop []
      (let [nt @next-timeout]
        (swap! next-timeout (fn [_] nil))
        (<! (timeout (if (= nil nt) 7000 nt))))
      (log/debug "identifying")
      (.identifyCancel mids)
      (.identifyAlbum mids)
      (recur))
    (.audioProcessStart mids (AudioSource. (ffmpeg-stream uri)))))

(defn load-locale [^com.gracenote.gnsdk.GnUser user]
  (let [locale (com.gracenote.gnsdk.GnLocale.
                com.gracenote.gnsdk.GnLocaleGroup/kLocaleGroupMusic
                com.gracenote.gnsdk.GnLanguage/kLanguageEnglish
                com.gracenote.gnsdk.GnRegion/kRegionDefault
                com.gracenote.gnsdk.GnDescriptor/kDescriptorDefault
                user)]))

(defn user-store [] (reify com.gracenote.gnsdk.IGnUserStore
                  (loadSerializedUser [_ clientId]
                    (try
                      (GnString. ^String (slurp (str "data/" clientId)))
                      (catch java.io.FileNotFoundException e nil)))
                  (storeSerializedUser [_ clientId serializedUser]
                    (spit (str "data/" clientId) serializedUser)
                    true)))

(defn user! [user-store] (let [user (com.gracenote.gnsdk.GnUser. user-store client-id client-tag VERSION)]
                           (.. user (options) (lookupMode com.gracenote.gnsdk.GnLookupMode/kLookupModeOnline ))
             (load-locale user)
             user))

(defn initialize-local-database []
  (let [storage (GnStorageSqlite/enable)]
    (.storageLocation storage "./data/cache/")
    (GnLookupLocal/enable)
    (GnLookupLocalStream/enable)))

(def config {:sky {:url "http://8623.live.streamtheworld.com/SKYRADIOAAC_SC"
                   :client-id ""
                   :client-tag ""
                   :client-license ""}
             :3fm {:url "http://icecast.omroep.nl/3fm-bb-mp3"
                   :client-id ""
                   :client-tag ""
                   :client-license ""}})

(defn merge-config [])

(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread throwable]
     (log/error throwable "Stacktrace:"
                (print-str (clojure.stacktrace/print-stack-trace throwable))))))

(defn -main [& args]
  (log/debug "java.library.path=" (System/getProperty "java.library.path"))
  (clojure.lang.RT/loadLibrary "gnsdk_java_marshal")
  (GnManager. gnsdk-lib client-license GnLicenseInputMode/kLicenseInputModeString)
  (initialize-local-database)
  (let [user (user! (user-store))]
    (server/run)
    (handle-song-channel song-channel)
    (go-loop []
      (<! (push-onto-stream seven-seconds-chan (make-logger :sky) (ffmpeg-stream "http://8623.live.streamtheworld.com/SKYRADIOAAC_SC")))
      (recur))
    (go-loop []
      (<! (push-onto-stream seven-seconds-chan (make-logger :3fm) (ffmpeg-stream "http://icecast.omroep.nl/3fm-bb-mp3")))
      (recur))
    (log/spyf :error "exited: %s" (<!! (stream-music1 seven-seconds-chan user)))))
