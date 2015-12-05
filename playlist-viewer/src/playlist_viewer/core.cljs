(ns playlist-viewer.core
  (:require [clojure.browser.repl :as repl]))

;; (defonce conn
;;   (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)


(def *current-playlist* (atom nil) )

(def all-playlists [
     {:name ""
      :tracks [
               {:artist "" :album "" :title "" :filename "" :running-time "" }
               {:artist "" :album "" :title "" :filename "" :running-time "" }
               ]}
     {:name "" :tracks 2}
  ])

(println "Hello world!")
