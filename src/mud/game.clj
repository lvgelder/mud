(ns mud.game
  (:require
            [ring.util.response :as response]
            [mud.models :as models]
            [mud.brain :as brain]
            [mud.chat :as chat]
            [mud.views :as views]
            [cemerick.friend :as friend]
            [mud.core :as core]))


(defn player [req]
  (let [identity (friend/identity req)
        player (core/get-player-from-identity identity)
        pl (models/player-by-id (:id player))
        room (models/room-by-player-id (:id player))
        other-players (chat/list-players pl (:id room))]
    (-> (response/response (views/player-page pl room (str (:flash req) other-players)))
        (response/header "X-Clacks-Overhead" "GNU Terry Pratchett"))))

(defn action [req]
  (let [identity (friend/identity req)
        player (core/get-player-from-identity identity)
        room (models/room-by-player-id (:id player))
        action (brain/action (:id player) (:action (:params req)) (:id room))]
    (assoc (response/redirect "/player") :flash action)))


