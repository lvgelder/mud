(ns mud.models
  (:refer-clojure) ;;(1)
  (:use korma.db korma.core)
  (:require [clojure.string :as string]))

(defdb mud ;;(2)
       (sqlite3 {:db "mud.db"}))

(defentity treasure
           (entity-fields :id :name :worth))

(defentity weapon
           (entity-fields :id :name :damage))

(declare player room)

(defentity monster
           (entity-fields :id :name :description :hit_points)
           (has-one weapon)
           (many-to-many treasure :monster_treasure)
           (many-to-many room :room_monster))

(defentity room
           (entity-fields :description :id)
           (many-to-many monster :room_monster)
           (many-to-many treasure :room_treasure)
           (many-to-many player :room_player))

(defentity player
           (entity-fields :id :name :description :hit_points )
           (has-one weapon)
           (many-to-many room :room_player)
           (many-to-many treasure :player_treasure)
           (many-to-many monster :player_monster)
           )

(defentity exit (entity-fields :id :from_room :to_room :description :locked))

(defentity room_player (entity-fields :player_id :room_id))

(defentity room_monster (entity-fields :monster_id :room_id))

(defentity player_monster (entity-fields :player_id :monster_id))

(defentity player_treasure (entity-fields :player_id :treasure_id))


(defn all-players []
  (select player))

(defn create-room [rm]
  (insert room (values rm))
  )

(defn create-player [pl]
  (insert player (values pl))
  )

(defn initialize-player-room [player_id room_id]
  (insert room_player
          (values {:room_id room_id :player_id player_id}))
  )

(defn set-player-room [player_id room_id]
  (update room_player
          (set-fields {:room_id room_id})
          (where {:player_id player_id}))
  )

(defn kill-monster [player_id monster_id]
  (insert player_monster
          (values {:monster_id monster_id :player_id player_id})))

(defn create-exit [ex]
  (insert exit (values ex)))

(defn room-by-id [id]
  (first (select room (with treasure) (with monster) (where {:id id}))))

(defn player-by-id [id]
  (first (select player (with treasure) (with monster) (where {:id id}))))

(defn room-by-player-id [pl_id]
  (first (select room
                 (with monster)
                 (join room_player
                       (= :room_player.room_id :id))
                 (where {:room_player.player_id pl_id})
                 )))

(defn monster-by-room [room_id]
  (first (select monster
                 (join room_monster
                       (= :room_monster.monster_id :id))
                 (where {:room_monster.room_id room_id})
                 )))

(defn player-by-name [name]
  (first (select player (where {:name name}))))

(defn exits-by-room [room_id]
  (select exit
          (where {:from_room room_id})
          (order :id)))

(defn monsters_killed [player_id monster_id]
  (select player_monster
          (where {:player_id player_id :monster_id monster_id})
          ))

(defn collect-treasure [player_id treasure_id]
  (insert player_treasure
          (values {:treasure_id treasure_id :player_id player_id})))