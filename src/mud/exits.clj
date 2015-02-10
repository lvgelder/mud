(ns mud.exits
  (:require
    [mud.models :as models]
    [mud.core :as core]
    ))

(defn list-exits [player-id _ room-id]
  (let [player (models/player-by-id player-id)
        exits (models/exits-by-room room-id)
        monsters (models/monster-by-room room-id)]
    (if (core/monsters-left-to-kill? player monsters)
      (format "You can't tell if there is a door because there is a %s trying to eat you." (:name (first monsters)))
      (apply str (concat (format "<p>You see %s exits:</p> " (count exits)) (map #(str (:description %)) exits)))
      )
    )
  )


(defn take-exit [player-id action room-id]
  (let [player (models/player-by-id player-id)
        exits (models/exits-by-room room-id)
        monsters (models/monster-by-room room-id)
        ]
    (cond
      (core/monsters-left-to-kill? player monsters) (format "You can't reach the door because there is a %s trying to eat you." (:name (first monsters)))
      (= (:locked (first exits)) 1) "This door is locked. You can't open it."
      :else (models/set-player-room player-id (:to_room (first exits)))
      )
    )
  )