(ns streamparser
  (:require [environ.core :refer [env]]
            [me.raynes.conch :as c]
            [me.raynes.conch.low-level :as sh]
            [clojure.core.async :refer [chan go go-loop <! <!! >! >!! sliding-buffer timeout thread]])
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
  (assoc
   (sh/proc "ffmpeg" "-i" url "-f" "wav" "-ar" "44100" "-t" "3600" "pipe:")
   :url url))

(defn take-from-stream [c ffmpeg-process]
  (thread (loop []
            (let [data-size 1234800
                  from (:out ffmpeg-process)
                  bytes (byte-array data-size)]
              (loop [total 0]
                (let [read (.read from bytes total (- data-size total))
                      new-total (+ total read)]
                  (if (>= new-total data-size)
                    (>!! c {:bytes bytes
                            :read  (min data-size new-total)
                            :url   (:url ffmpeg-process)})
                    (recur new-total)))))
            (println "@@@ delivered 7 seconds for: " (:url ffmpeg-process))
            (recur))))

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

(defn handle-result [result]
  (let [albums (-> (.. result (albums) (getIterator))
                   (gn-iterator->iterator-seq))
        result (-> (map convert-result albums)
                   doall
                   first)]
    (swap! next-timeout (fn [_] 7000;; (max 30000
                                ;;     (- (:track-duration result 0)
                                ;;        (:match-position result 0)))
                          ))
    (println result)))

(defn make-logger [] (reify
                       IGnMusicIdStreamEvents
                       (musicIdStreamProcessingStatusEvent [_ status _]
                         ;; (println status)
                         )
                       (musicIdStreamIdentifyingStatusEvent [_ status c]
                         (println ">>> " status)
                         (if (= status GnMusicIdStreamIdentifyingStatus/kStatusIdentifyingEnded)
                           (.setCancel c true)))
                       (musicIdStreamAlbumResult [_ result _]
                         (handle-result result))
                       (musicIdStreamIdentifyCompletedWithError [_ error]
                         ;; (println error)
                         )
                       (statusEvent [_ _ percent _ _ _]
                         ;; (println percent)
                         )))

(defn stream-music1 [c user logger]
  (thread (try (loop []
                 (let [msg (<!! c)
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
  (let [mids (GnMusicIdStream. user GnMusicIdStreamPreset/kPresetRadio (make-logger))]
    (.. mids (options) (resultSingle true))
    (.. mids (options) (lookupData GnLookupData/kLookupDataExternalIds true))
    (.. mids (options) (lookupData GnLookupData/kLookupDataGlobalIds true))
    (doto mids
      (.automaticIdentifcation false))
    (go-loop []
      (let [nt @next-timeout]
        (swap! next-timeout (fn [_] nil))
        (<! (timeout (if (= nil nt) 7000 nt))))
      (println "call identify")
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
    (.storageLocation storage "/Users/erwin/Develop/monkeyregal/clojurecup2015/streamparser/data/cache/")
    (GnLookupLocal/enable)
    (GnLookupLocalStream/enable)))

(defn -main [& args]
  (println (System/getProperty "java.library.path"))
  (clojure.lang.RT/loadLibrary "gnsdk_java_marshal")
  (GnManager. gnsdk-lib client-license GnLicenseInputMode/kLicenseInputModeString)
  (initialize-local-database)
  (let [user (user! (user-store))
        logger (make-logger)]
    (take-from-stream seven-seconds-chan (ffmpeg-stream "http://8623.live.streamtheworld.com/SKYRADIOAAC_SC"))
    (take-from-stream seven-seconds-chan (ffmpeg-stream "http://icecast.omroep.nl/3fm-bb-mp3"))
    (println (<!! (stream-music1 seven-seconds-chan user logger)))
    ))
