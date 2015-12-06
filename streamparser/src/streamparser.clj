(ns streamparser
  (:require [environ.core :refer [env]]
            [me.raynes.conch :as c]
            [me.raynes.conch.low-level :as sh])
  (:import  [com.gracenote.gnsdk
             GnManager
             GnLicenseInputMode
             GnLocale
             GnLocaleGroup
             GnRegion
             GnDescriptor
             GnUser
             IGnUserStore
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
             GnMusicIdStreamPreset])
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

(defn ffmpeg-stream [url]
  (sh/proc "ffmpeg" "-i" url "-f" "wav" "-ar" "44100" "-t" "7" "pipe:"))

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

(defn make-logger [] (reify
                    IGnMusicIdStreamEvents
                    (musicIdStreamProcessingStatusEvent [_ _ _])
                    (musicIdStreamIdentifyingStatusEvent [_ _ _])
                    (musicIdStreamAlbumResult [_ result _]
                      (println result))
                    (musicIdStreamIdentifyCompletedWithError [_ _])
                       (statusEvent [_ _ _ _ _ _])))

(defn stream-music [uri user]
  (let [mids (GnMusicIdStream. user GnMusicIdStreamPreset/kPresetRadio (make-logger))]
    (.. mids (options) (resultSingle true))
    (doto mids
      (.automaticIdentifcation true)
      (.audioProcessStart (AudioSource. (ffmpeg-stream uri)))
      (.identifyAlbum))))

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
             (.. user (options) (lookupMode com.gracenote.gnsdk.GnLookupMode/kLookupModeOnline))
             (load-locale user)
             user))

(defn -main [& args]
  (println (System/getProperty "java.library.path"))
  (System/loadLibrary "gnsdk_java_marshal")
  (GnManager. gnsdk-lib client-license GnLicenseInputMode/kLicenseInputModeString)
  (let [user (user! (user-store))]
    (stream-music "http://icecast.omroep.nl/3fm-bb-mp3" user)))
