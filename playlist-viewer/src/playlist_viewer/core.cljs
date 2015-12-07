(ns playlist-viewer.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [playlist-viewer.global :refer [app-state]]
    [playlist-viewer.views :refer [container render-playlist render-all-playlists]]
    [cljs-http.client :as http]
    [cljs-http.util :as util]
    [cljs.core.async :refer [timeout <!]]))

(enable-console-print!)
(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload


(defn hello-world []
  [:h1 (:text @app-state)])


(reagent/render-component [container]
                          (. js/document (getElementById "app")))

(go-loop []
  (let [response (<! (http/get "http://nodejsplz.monkeyregal.com/last"))
        decoded  (util/json-decode response)]
    (swap! app-state (fn [prev] (update-in prev [:playlists 0 :tracks] decoded)))
    (<! (timeout 15000)))
  (recur))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
