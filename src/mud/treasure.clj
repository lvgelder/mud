(ns mud.treasure
  (:require
    [mud.models :as models]
    [clojure.string :as str]))

; search with no args
; if room has treasure call list-treasure-from room
; if room has monster call list-treasure-from-monster
; if no treasure or monster say there is nothing to find

;search with args
; if actions contains both search and room, call list-treasure-from-room
; if room has monster and actions matches monster name call list-treasure-from-monster
; otherwise say we don't know what to search for

;get-treasure-from-room
;if room has treasure return it
;if no treasure say that

;get-treasure-from-monster
;if monster is dead and has treasure, return that
;if monster is dead but has no treasure say that
; if monster not dead, warn that the monster tries to eat you while you search
; call combat/monster-attack-player

;remove-item
;remove item from player-treasure

;can collect treasure?
; if player has more than 5 items can't pick up more treasure

; do we need an explicit mapping to key id for how to unlock the door? Or just a key?
; when you use a key call remove-item to remove it from player items

;list player items properly

;todo - room has multiple treasures and player must choose one

(defn seq-contains? [coll target] (some #(= target %) coll))

(defn not-killed-monster[player-id monster-id]
  (let [killed-monster (models/monsters_killed player-id monster-id)]
    (empty? killed-monster)
    )
  )

(defn seq-contains-id? [coll target] (some #(= (:id target) (:id %)) coll))

(defn contains-item-with-id [items item]
  (seq-contains-id? items item)
  )

(defn list-treasure-in-room [player-id action room-id]

  (defn treasure-item[name]
    (str "<li>" name "</li>")
    )

  (let [room (models/room-by-id room-id) treasure (:treasure room) monsters (:monster room)
        not-killed-monsters (filter #(not-killed-monster player-id (:id %)) monsters)
        ]
    (if (not (empty? not-killed-monsters))
      (format "You try to search the room but the %s tries to eat you..." (:name (first not-killed-monsters)))
      (format "<p>You see %s items in this room.</p> <ul>%s</ul>"
              (count treasure) (reduce str (map #(treasure-item (:description %)) treasure)))
      )
    )
  )

(defn get-treasure-from-room [player-id action room-id]
(let [room (models/room-by-id room-id)]
  (if (not (empty? (:treasure room)))
    (models/collect-treasure player-id (:id (first (:treasure room))) )
    "There is no treasure in this room."
    )
  )
  )

(defn take-item-from-room [player-id action room-id]
  (let [room (models/room-by-id room-id) treasure (:treasure room) action-list (str/split action #" ")
        treasure-to-take (filter #(seq-contains? action-list (:name %)) treasure)
        monsters (:monster room)
        player (models/player-by-id player-id)
        taken-treasure (filter #(contains-item-with-id (:treasure player) %) treasure-to-take)
        killed-monsters (filter #(contains-item-with-id (:monster player) %) monsters)
        ]
    (cond
      (empty? treasure-to-take) "You can't take that."
      (and (not (empty? monsters)) (empty? killed-monsters)) (str (format "You can't take it because the %s tries to eat you." (:name (first monsters))))
      (not (empty? taken-treasure)) "You already have that."
      :else (
              do
              (models/collect-treasure player-id (:id (first treasure-to-take)))
              (str (format "You have the %s." (:name (first treasure-to-take))))
              )
      )
    )
  )




