(ns mud.messages
  (:use compojure.core
                       ring.util.response
                       ring.middleware.cors
                       org.httpkit.server)
  (:require
            [cemerick.friend :as friend]
            [cheshire.core :refer :all]
            [mud.core :as core]))

(def players (atom {}))

(defn ws
  [req]
  (let [identity (friend/identity req)
        player (core/get-player-from-identity identity)]
    (with-channel req con
                  (swap! players assoc (:id player) con)
                  (println (:id player) " connected")
                  (on-close con (fn [status]
                                  (swap! players dissoc con)
                                  (println con " disconnected. status: " status))))))

(defn messsage [message player-id]
  (send! (@players player-id) (generate-string {:message message})))

(defn currently-playing [player-id]
  (contains? @players player-id))

;(future (loop []
;          (doseq [playerid (keys @players)]
;            (println playerid)
;            (send! (@players playerid) (generate-string
;                                  {:message "Bob has taken the scone."})
;                   false))
;          (Thread/sleep 5000)
;          (recur)))