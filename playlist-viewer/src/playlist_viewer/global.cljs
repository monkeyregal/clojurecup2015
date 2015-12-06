(ns playlist-viewer.global
  (:require
    [reagent.core :as reagent :refer [atom]]))

(defonce app-state (atom
                     {:current-playlist 0
                      :playlists
                        [ {
                           :name "First Playlist"
                           :created "2015-12-06"
                           :tracks [
                            {:name "Track 1"
                             :artist "Dizzy Gillespie"
                             :album "It's all Jazz"
                             :meta {}}
                            {:name "Track 2"
                             :artist "Dizzy Gillespie"
                             :album "It's all Jazz"
                             :meta {}}
                            {:name "Track 3"
                             :artist "Dizzy Gillespie"
                             :album "It's all Jazz"
                             :meta {}}]
                           }
                         {:name "Second Playlist"
                          :created "2015-12-06"
                          :tracks []}
                         {:name "Third Playlist"
                          :created "2015-12-06"
                          :tracks []}
                         ]}))
