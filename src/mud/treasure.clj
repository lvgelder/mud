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

(defn list-treasure-in-room [player-id action room-id]

  (defn treasure-item[name]
    (str "<li>" name "</li>")
    )

  (let [room (models/room-by-id room-id)
        treasure (:treasure room)
        player (models/player-by-id player-id)
        treasure-left-in-room (core/treasure-left player treasure)
        monsters (:monster room)
        ]
    (if (core/monsters-left-to-kill? player monsters)
      (format "You try to search the room but the %s tries to eat you..." (:name (first monsters)))
      (format "<p>You see %s items in this room.</p> <ul>%s</ul>"
              (count treasure-left-in-room) (reduce str (map #(treasure-item (:description %)) treasure-left-in-room)))
      )
    )
  )

(defn search [player-id action room-id]
  (let [room (models/room-by-id room-id)
        player (models/player-by-id player-id)
        monsters-left-to-kill (core/monsters-left-to-kill player (:monster room))]
    (cond
      (not (empty? monsters-left-to-kill)) (format "You try to search but the %s tries to eat you..." (:name (first monsters-left-to-kill)))
      (= action "search") (list-treasure-in-room player-id action room-id)
      )
    )
  )

(defn has-five-items-or-more[player]
  (>= (count (:treasure player)) 5)
  )

(defn take-item-from-room [player-id action room-id]
  (let [room (models/room-by-id room-id)
        treasure (:treasure room)
        treasure-to-take (first (core/treasure-mentioned action treasure))
        monsters (:monster room)
        player (models/player-by-id player-id)
        ]
    (cond
      (empty? treasure-to-take) "You can't take that."
      (core/monsters-left-to-kill? player monsters) (str (format "You can't take it because the %s tries to eat you." (:name (first monsters))))
      (core/already-taken-treasure? player treasure-to-take) "You already have that."
      (has-five-items-or-more player) "You already have 5 items. You need to drop something."
      :else (
              do
              (models/collect-treasure player-id (:id treasure-to-take))
              (str (format "You have the %s." (:name treasure-to-take)))
              )
      )
    )
  )

(defn drop-item [player-id action room-id]
  (let [player (models/player-by-id player-id)
        treasure (:treasure player)
        droppable-treasure (core/treasure-mentioned action treasure)]
    (if (empty? droppable-treasure)
      "You don't have that."
      (do
        (models/remove-treasure-from-player (:id player) (:id (first droppable-treasure)))
        (str (format "You put the %s down." (:name (first droppable-treasure))))
        )

      )

    )
  )




