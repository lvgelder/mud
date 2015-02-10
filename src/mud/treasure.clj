(ns mud.treasure
  (:require
    [mud.models :as models]
    [mud.core :as core]
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

(defn list-treasure-in-room [player-id action room-id]

  (defn treasure-item[name]
    (str "<li>" name "</li>")
    )

  (let [room (models/room-by-id room-id)
        treasure (:treasure room)
        monsters (:monster room)
        player (models/player-by-id player-id)
        ]
    (if (core/monsters-left-to-kill player monsters)
      (format "You try to search the room but the %s tries to eat you..." (:name (first monsters)))
      (format "<p>You see %s items in this room.</p> <ul>%s</ul>"
              (count treasure) (reduce str (map #(treasure-item (:description %)) treasure)))
      )
    )
  )

(defn take-item-from-room [player-id action room-id]
  (let [room (models/room-by-id room-id) treasure (:treasure room) action-list (str/split action #" ")
        treasure-to-take (first (filter #(core/seq-contains? action-list (:name %)) treasure))
        monsters (:monster room)
        player (models/player-by-id player-id)
        ]
    (cond
      (empty? treasure-to-take) "You can't take that."
      (core/monsters-left-to-kill player monsters) (str (format "You can't take it because the %s tries to eat you." (:name (first monsters))))
      (core/already-taken-treasure player treasure-to-take) "You already have that."
      :else (
              do
              (models/collect-treasure player-id (:id treasure-to-take))
              (str (format "You have the %s." (:name treasure-to-take)))
              )
      )
    )
  )




