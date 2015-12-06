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
                           :name "My Jazz Favourites"
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
                             :meta {}}
                            {:name "Track 4"
                             :artist "Dizzy Gillespie"
                             :album "It's all Jazz"
                             :meta {}}
                            {:name "Track 5"
                             :artist "Dizzy Gillespie"
                             :album "It's all Jazz"
                             :meta {}}
                            {:name "Track 6"
                             :artist "Dizzy Gillespie"
                             :album "It's all Jazz"
                             :meta {}}
                            {:name "Track 7"
                             :artist "Dizzy Gillespie"
                             :album "It's all Jazz"
                             :meta {}}
                            {:name "Track 8"
                             :artist "Dizzy Gillespie"
                             :album "It's all Jazz"
                             :meta {}}
                            {:name "Track 9"
                             :artist "Dizzy Gillespie"
                             :album "It's all Jazz"
                             :meta {}}
                            {:name "Track 10"
                             :artist "Dizzy Gillespie"
                             :album "It's all Jazz"
                             :meta {}}
                                    ]
                           }
                         {:name "Second Playlist"
                          :created "2015-12-06"
                          :tracks []}
                         {:name "Third Playlist"
                          :created "2015-12-06"
                          :tracks []}
                         ]}))
