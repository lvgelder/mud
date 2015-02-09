(ns mud.treasure
  (:require
    [mud.models :as models]
    ))

; search with no args
; if room has treasure call get-treasure-from room
; if room has monster call get-treasure-from-monster
; if no treasure or monster say there is nothing to find

;search with args
; if actions contains both search and room, call get-treasure-from-room
; if room has monster and actions matches monster name call get-treasure-from-monster
; otherwise say we don't know what to search for

;get-treasure-from-room
;if room has treasure return it
;if no treasure say that

;get-treasure-from-monster
;if monster is dead and has treasure, return that
;if monster is dead but has no treasure say that
; if monster not dead, warn that the monster tries to eat you while you search
; call combat/monster-attack-player

;discard-item
;remove item from player-treasure

;can collect treasure?
; if player has more than 5 items can't pick up more treasure

;todo - room has multiple treasures and player must choose one

(defn get-treasure-from-room [player-id action room-id]
(let [room (models/room-by-id room-id)]
  (if (not (empty? (:treasure room)))
    (models/collect-treasure player-id (:id (first (:treasure room))) )
    "There is no treasure in this room."
    )
  )
  )





