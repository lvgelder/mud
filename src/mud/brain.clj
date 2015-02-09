(ns mud.brain
  (:require
    [mud.combat :as combat]
    [mud.exits :as exits]
    [clojure.string :as str]
    ))




(defn search [player_id action room_id]

  )

(defn action [player-id action room-id]
  (let [actions {"exits" exits/list-exits "doors" exits/list-exits
                 "open" exits/take-exit "fight" combat/fight}
        action-list (str/split action #" ")
        possible-actions (filter #(contains? actions %) action-list)
        ]

    (if (empty? possible-actions)
      "I don't know how to do that"
      ((actions (first possible-actions)) player-id action room-id)
      )
    )
  )

