(ns playlist-viewer.views
  (:require
    [playlist-viewer.global :refer [app-state]]))


(defn play-sound []
  (.play
    (.createElement js/document "AUDIO") "sfx/spider_monkey_screech_or_howl.mp3"))


(defn add-stream [])


(defn add-track [])

(defn navbar []
  [:nav {:class "navbar navbar-default"}
   [:a {:class "navbar-brand" :href "#" } "Regal Monkey" ]
   [:button {:class "btn btn-info btn-navbar"
             :on-click #(add-stream)
             } "+ Stream"]
   [:button {:class "btn btn-info btn-navbar"
             :on-click #(add-track)
             } "+ Track"]
   [:img {:class "navbar-right rm-nav-image"
          :on-click #(play-sound)
          :src "sfx/Monkey-37-277x300.jpg"}]
   ])

(defn render-playlist-line [item]
  [:div {:class ""} (:name item)
   [:span {:class "badge rm-badge-spacer"} (count (:tracks item))]])


(defn render-all-playlists []
  (map
    #(render-playlist-line %)
    (:playlists @app-state)))


(defn render-track [track]
  [:div {:class ""}
   [:div {:class ""} (:name track)]
   [:div {:class ""} (:artist track)]
   [:div {:class ""} (:album track)]])

(defn render-tracks [tracks]
  (map #(render-track %) tracks))


(defn render-playlist-item [item]
  [:div {:class ""} (:name item)
   (render-tracks (:tracks item))])


(defn render-playlist []
  (render-playlist-item
    (nth
      (:playlists @app-state)
      (:current-playlist @app-state))))


(defn container []
    [:div {:class "container"}
     [:div (navbar)]
     [:div {:class "row"}
      [:div {:id "left" :class "col-md-9"} (render-playlist)]
      [:div {:id "right" :class "col-md-3"} (render-all-playlists)]]])

