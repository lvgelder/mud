(ns mud.models
  (:refer-clojure) ;;(1)
  (:use korma.db korma.core)
  (:require [clojure.string :as string]))

(defdb mud ;;(2)
       (sqlite3 {:db "mud.db"}))

(defentity treasure
           (entity-fields :id :name :worth))

(defentity monster
           (entity-fields :id :name :description :weapon :hit_points) (many-to-many treasure :monster_treasure))

(defentity room
           (entity-fields :id :description) (many-to-many monster :room_monster))

(defentity exit (entity-fields :id :from_room :to_room :description))

(defentity player
           (entity-fields :id :name :description :weapon :hit_points))

(defentity weapon
           (entity-fields :id :name :damage))

(defn create-room [rm]
  (insert room (values rm)))

(defn create-exit [ex]
  (insert exit (values ex)))

(defn room-by-id [id]
  (first (select room (where {:id id}))))

(defn exits-by-room [room_id]
  (select exit
          (where {:from_room room_id})
          (order :id)))