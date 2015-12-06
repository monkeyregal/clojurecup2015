(ns streamparser
  (:require [me.raynes.conch :as c]
            [boot.pod :as pod])
  (:import  [com.gracenote.gnsdk.GnManager]))

(defn -main [])
(defonce static-load
  (do
    (println (System/getProperty "java.library.path"))
    (System/loadLibrary "gnsdk_java_marshal")))

;;(def manager (GnManager. ))
