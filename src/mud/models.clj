(ns mud.models
  (:refer-clojure)
  (:use korma.db korma.core)
  (:require [environ.core :refer [env]]
            [cemerick.friend.credentials :as creds]))

(def database-name
  (env :database-name))

(def database-username
  (env :database-username))

(def database-password
  (env :database-password))

(def database-host
  (env :database-host))

(def database-port
  (env :database-port))

(defdb mud (postgres {:db database-name
                       :user database-username
                       :password database-password
                       :host database-host
                       :port database-port
                       :delimiters ""}))

(defentity treasure
           (entity-fields :id :name :description :type :action_description :hit_points))

(declare player room monster user_role friend_group player_friend_group friend_group_treasure)

(defentity weapon
           (entity-fields :id :name :damage))

(defentity monster
           (entity-fields :id :name :description :hit_points)
           (many-to-many treasure :monster_treasure)
           (many-to-many weapon :monster_weapon)
           (many-to-many room :room_monster))

(defentity room
           (entity-fields :description :id)
           (many-to-many monster :room_monster)
           (many-to-many treasure :room_treasure)
           (many-to-many player :room_player))

(defentity player
           (entity-fields :id :name :hit_points :max_hit_points)
           (many-to-many room :room_player)
           (many-to-many treasure :player_treasure)
           (many-to-many monster :player_monster)
           (many-to-many weapon :player_weapon)
           (many-to-many friend_group :player_friend_group))

(defentity mud_role (entity-fields :id :name))

(defentity mud_user
           (entity-fields :id :username :password)
           (many-to-many mud_role :user_role)
           (many-to-many player :user_player))

(defentity friend_group
           (entity-fields :id :name)
           (many-to-many treasure :friend_group_treasure))

(defentity player_friend_group
           (entity-fields :friend_group_id :player_id))

(defentity user_player (entity-fields :mud_user_id :player_id))

(defentity user_role (entity-fields :mud_user_id :mud_role_id))

(defentity exit (entity-fields :id :from_room :to_room :description :keywords :locked))

(defentity room_player (entity-fields :player_id :room_id))

(defentity room_monster (entity-fields :monster_id :room_id))

(defentity player_monster (entity-fields :player_id :monster_id))

(defentity player_treasure (entity-fields :player_id :treasure_id))

(defentity eaten_treasure (entity-fields :player_id :treasure_id))

(defentity worn_treasure (entity-fields :player_id :treasure_id))

(defentity fight_in_progress (entity-fields :player_id :monster_id :monster_hit_points))

(defentity player_weapon (entity-fields :player_id :weapon_id))

(defentity monster_weapon (entity-fields :monster_id :weapon_id))

(defentity combinable_treasure (entity-fields :combined_treasure_id :treasure_id))

(defentity friend_group_treasure (entity-fields :friend_group_id :treasure_id))

(defn create-user [usr]
  (insert mud_user
          (values {:username (:username usr) :password (creds/hash-bcrypt (:password usr))})))

(defn add-user-role [user-id role-id]
  (insert user_role
          (values {:mud_user_id user-id  :mud_role_id role-id})))

(defn find-by-username [username]
  (first (select mud_user (with player) (where {:username username}))))

(defn add-user-player [user-id player-id]
  (insert user_player
          (values {:mud_user_id user-id :player_id player-id})))

(defn create-player [pl]
  (insert player
          (values {:name (:name pl) :description "The Hero"})))

(defn create-friend-group [f]
  (insert friend_group
          (values {:name (:name f)})))

(defn update-friend-group [friend-group-id name]
  (update friend_group
          (set-fields {:name name})
          (where {:id friend-group-id})))

(defn add-player-to-friend-group [player-id friend-group-id]
  (insert player_friend_group
          (values {:player_id player-id :friend_group_id friend-group-id})))

(defn remove-player-from-friend-group [player-id friend-group-id]
  (delete player_friend_group
          (where {:player_id player-id :friend_group_id friend-group-id})))

(defn friend-group-by-name [name]
  (first (select friend_group (where {:name name}))))

(defn friend-group-by-id [id]
  (first (select friend_group (with treasure) (where {:id id}))))

(defn players-by-friend-group [friend_group_id]
  (select player
          (join player_friend_group
                (= :player_friend_group.player_id :id))
          (where {:player_friend_group.friend_group_id friend_group_id})))

(defn collect-treasure-for-friend-group [friend_group_id treasure_id]
  (insert friend_group_treasure
          (values {:treasure_id treasure_id :friend_group_id friend_group_id})))

(defn initialize-player-room [player_id room_id]
  (insert room_player
          (values {:room_id room_id :player_id player_id})))

(defn initialize-player-weapon [player_id]
  (insert player_weapon
          (values {:weapon_id 1 :player_id player_id})))

(defn set-player-room [player_id room_id]
  (update room_player
          (set-fields {:room_id room_id})
          (where {:player_id player_id})))

(defn set-hit-points [player_id hit_points]
  (update player
          (set-fields {:hit_points hit_points})
          (where {:id player_id})))

(defn set-max-hit-points [player_id hit_points]
  (update player
          (set-fields {:max_hit_points hit_points})
          (where {:id player_id})))

(defn kill-monster [player_id monster_id]
  (insert player_monster
          (values {:monster_id monster_id :player_id player_id})))

(defn room-by-id [id]
  (first (select room (with treasure) (with monster) (where {:id id}))))

(defn player-by-id [id]
  (first (select player (with treasure) (with monster) (with friend_group) (where {:id id}))))

(defn player-with-weapon [id]
  (first (select player (with weapon) (where {:id id}))))

(defn monster-by-id [id]
  (first (select monster (with treasure) (where {:id id}))))

(defn monster-with-weapon [id]
  (first (select monster (with weapon) (where {:id id}))))

(defn room-by-player-id [pl_id]
  (first (select room
                 (with monster)
                 (join room_player
                       (= :room_player.room_id :id))
                 (where {:room_player.player_id pl_id}))))

(defn monster-by-room [room_id]
  (select monster
                 (join room_monster
                       (= :room_monster.monster_id :id))
                 (where {:room_monster.room_id room_id})))

(defn player-by-name [name]
  (first (select player (with room) (where {:name name}))))

(defn exits-by-room [room_id]
  (select exit
          (where {:from_room room_id})
          (order :id)))

(defn collect-treasure [player_id treasure_id]
  (insert player_treasure
          (values {:treasure_id treasure_id :player_id player_id})))

(defn remove-treasure-from-player [player_id treasure_id]
  (delete player_treasure
          (where {:player_id player_id :treasure_id treasure_id})))

(defn eat-treasure [player_id treasure_id]
  (insert eaten_treasure
          (values {:treasure_id treasure_id :player_id player_id})))

(defn wear_treasure [player_id treasure_id]
  (insert worn_treasure
          (values {:treasure_id treasure_id :player_id player_id})))

(defn eaten-treasure-by-player-id [pl_id]
  (select treasure
                 (join eaten_treasure
                       (= :eaten_treasure.treasure_id :id))
                 (where {:eaten_treasure.player_id pl_id})))

(defn worn-treasure-by-player-id [pl_id]
  (select treasure
          (join worn_treasure
                (= :worn_treasure.treasure_id :id))
          (where {:worn_treasure.player_id pl_id})))

(defn find-combined-treasure [treasure-id]
  (select treasure
          (join combinable_treasure
                (= :combinable_treasure.combined_treasure_id :id))(where {:combinable_treasure.treasure_id treasure-id})))


(defn find-combinable-items [treasure-id]
  (select treasure
                 (join combinable_treasure
                       (= :combinable_treasure.treasure_id :id))
                 (where {:combinable_treasure.combined_treasure_id (:id (first (find-combined-treasure treasure-id)))})))

(defn remove-all-treasure-from-player [player_id]
  (delete player_treasure
          (where {:player_id player_id})))

(defn remove-all-monsters-from-player [player_id]
  (delete player_monster
          (where {:player_id player_id})))

(defn reset-fight-in-progress [player_id]
  (delete fight_in_progress
          (where {:player_id player_id})))

(defn select_fight_in_progress [player_id monster_id]
  (select fight_in_progress
                 (where {:player_id player_id :monster_id monster_id})))

(defn insert_fight_in_progress [player_id monster_id monster_hit_points]
  (insert fight_in_progress
          (values {:monster_hit_points monster_hit_points :player_id player_id :monster_id monster_id})))

(defn update-fight-in-progress [player_id monster_id monster_hit_points]
  (update fight_in_progress
          (set-fields {:monster_hit_points monster_hit_points})
          (where {:player_id player_id :monster_id monster_id})))

(defn find-players-in-room [room-id]
  (select player (join room_player (= :room_player.player_id :id)) (where {:room_player.room_id room-id})))



