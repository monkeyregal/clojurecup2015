(ns playlist-viewer.global
  (:require
    [reagent.core :as reagent :refer [atom]]))


(defonce radio-stations
         (atom [
                {:name "3FM"
                 :lists [
                         {:name "Last 250" :url "https://play.spotify.com/user/monkeyregal/playlist/0Dk9ouTKwXzGpqkjVavDRl"}]}
                {:name "Sky Radio"
                 :lists [
                         {:name "Last 250" :url "https://play.spotify.com/user/monkeyregal/playlist/23xdXOfVgdQlDDc895COGM"} ]}
                {:name "NL"
                 :lists [
                         {:name "Last 250" :url "https://play.spotify.com/user/monkeyregal/playlist/0zENxOQG6nXtN0ZYfud7FW"} ]}
                ]))

(defonce app-state (atom
                     {:current-playlist 0
                      :playlists
                        [ {
                           :name "NL"
                           :tracks [
                                    ]
                           } 
                         ]}))
