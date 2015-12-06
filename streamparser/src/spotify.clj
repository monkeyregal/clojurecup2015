(ns spotify
  (:require [clj-http.client :as client]
            [clj-spotify.core :as spot]
            [clj-time.core :as t]
            [clojure.string :as str]
            [environ.core :refer [env]]))

(def spotify-accounts-url "https://accounts.spotify.com/api/token")

(def access (atom nil))

(defn refresh-auth-token []
  (when (or (not @access) (t/after? (t/now) (:valid-to @access)))
    (let [refresh-token (slurp (env :spotify-refresh-token-file))
          auth [(env :spotify-client-id) (env :spotify-client-secret)]
          form-params {:grant_type "refresh_token"
                       :redirect_uri (env :spotify-redirect-uri)
                       :refresh_token refresh-token}
          result (client/post spotify-accounts-url
                              {:basic-auth auth
                               :form-params form-params
                               :as :json
                               :throw-exceptions false})]
      (when (= 200 (:status result))
        (let [body (:body result)
              access-token (:access_token body)
              expires-in (- (:expires_in body 3600) 30)
              valid-to (t/plus (t/now) (t/seconds expires-in))]
          (swap! access (fn [_] {:token access-token :valid-to valid-to})))))))

(defn get-token []
  (refresh-auth-token)
  (:token @access))


(defn trim-playlist [owner playlist-id max-n]
  (let [current (spot/get-a-playlist {:owner_id owner
                                      :playlist_id playlist-id
                                      :fields "snapshot_id,tracks.total"}
                                     (get-token))
        total (get-in current [:tracks :total] 0)
        remove-n (- total max-n)
        snapshot-id (:snapshot_id current)
        ids (into [] (range 0 remove-n))]
    (when (seq ids)
      (spot/remove-tracks-from-a-playlist
       {:user_id owner
        :playlist_id playlist-id
        :snapshot_id snapshot-id
        :positions ids}
       (get-token)))))


(def forbidden-words ["featuring" "feat" "(" ")" "." ","])

(defn sanitize [name]
  (let [pattern (->> forbidden-words (map #(java.util.regex.Pattern/quote %))
                (interpose \|)  (apply str))]
    (.replaceAll (clojure.string/lower-case name) pattern "")))

(defn find-song [artist track-name]
  (let [sane-artist (sanitize artist)
        sane-track (sanitize track-name)
        result (spot/search {:market "NL"
                             :type "track"
                             :limit 1
                             :query (format "%s %s" sane-artist sane-track)}
                            (get-token))
        track (first (get-in result [:tracks :items]))]
    (when track
      (:uri track))))

(defn add-song [owner playlist-id artist track-name]
  (when-let [song (find-song artist track-name)]
    (spot/add-tracks-to-a-playlist {:user_id owner :playlist_id playlist-id
                                    :uris [song]} (get-token))))

;; (spot/get-a-playlists-tracks {:owner_id "monkeyregal" :playlist_id "77sZVoT6PXbrHixwNj0JQr" :fields "items(track(name,uri))"} (get-token))

;(spot/get-a-playlist {:owner_id "monkeyregal" :playlist_id "77sZVoT6PXbrHixwNj0JQr" :fields "snapshot_id,tracks.items(track(name,uri))"} (get-token))


; (spot/add-tracks-to-a-playlist {:user_id "monkeyregal" :playlist_id "77sZVoT6PXbrHixwNj0JQr" :uris ["spotify:track:4iV5W9uYEdYUVa79Axb7Rh"]} (get-token))
