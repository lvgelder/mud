(ns mud.friend_group
  (:require
    [ring.util.response :as response]
    [mud.models :as models]
    [mud.validations :as valid]
    [cemerick.friend :as friend]
    [mud.core :as core]
    [environ.core :refer [env]]
    [clojure.string :as str]
    [mud.views :as views]))

(defn valid-playernames [playernames]
  (let [player-list (str/split playernames #",")]
    (map #(models/player-by-name (str/trim %)) player-list)))

(defn save-new-friend-group [req]
  (let [identity (friend/identity req)
        current-player (core/get-player-from-identity identity)
        params (:params req)
        other-players (valid-playernames (:playernames params))
        fr (models/create-friend-group params)
        friend-group (models/friend-group-by-name (:name params))]
    (models/add-player-to-friend-group (:id current-player) (:id friend-group))
    (doall (for [player other-players]  (models/add-player-to-friend-group (:id player)(:id friend-group))))
    (response/redirect "/player")))

(defn save-updated-friend-group [req]
  (let [params (:params req)
        other-players (valid-playernames (:playernames params))]
    (doall (for [player other-players]  (models/add-player-to-friend-group (:id player)(read-string (:friend_group_id params)))))
    (response/redirect "/friend-group")))

(defn all-playernames-exist [playernames]
  (let [players (valid-playernames playernames)]
    (every? identity players)))

(defn make-friend-group [req]
  (let [params (:params req)
        err (valid/valid-friend-group? params)]
    (if (not(empty? err))
      (assoc (response/redirect "/friend-group/new") :flash (assoc err :form-vals {:playernames (:playernames params) :name (:name params)}))
      (if-not (all-playernames-exist (:playernames params))
        (assoc (response/redirect "/friend-group/new") :flash { :playernames ["Hero does not exist"] :form-vals {:playernames (:playernames params) :name (:name params)}} )
        (save-new-friend-group req)))))


(defn update-friend-group [req]
  (let [params (:params req)]
    (if-not (all-playernames-exist (:playernames params))
      (assoc (response/redirect "/friend-group") :flash { :playernames ["Hero does not exist"] :form-vals {:playernames (:playernames params) :name (:name params)}} )
      (save-updated-friend-group req))))

(defn friend-group [req]
  (let [identity (friend/identity req)
        pl (core/get-player-from-identity identity)
        player (models/player-by-id (:id pl))
        friend-group-id (:id (first (:friend_group player)))]
    (if-not friend-group-id
      (views/new-friend-group req)
      (views/edit-friend-group (assoc req :flash {:friend_group (models/friend-group-by-id friend-group-id)})))))

; check player is part of this friend group before allowing this action
(defn remove-player-from-friend-group [req]
  (let [params (:params req)
        friend_group_id (read-string (:friend_group_id params))
        player_id (read-string (:player_id params))
        identity (friend/identity req)
        player (core/get-player-from-identity identity)
        players-in-friend-group (models/players-by-friend-group friend_group_id)]
    (if-not (core/contains-item-with-id players-in-friend-group player)
      (assoc (response/redirect "/friend-group") :flash "Action not permitted.")
      ( do
        (models/remove-player-from-friend-group player_id friend_group_id)
        (response/redirect "/friend-group")))))
