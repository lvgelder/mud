(ns mud.brain
  (:require
    [mud.models :as models]
    [clojure.string :as str]
    ))


(defn list-exits [player-id action room-id]
  (let [exits (models/exits-by-room room-id)]
    (apply str (concat (format "<p>You see %s exits: </p>" (count exits)) (map #(str (:description %)) exits)))
    )
  )

(defn take-exit [player-id action room-id]
  (let [exits (models/exits-by-room room-id)]
    (models/set-player-room player-id (:to_room (first exits)))
    )
  )

(defn action [player-id action room-id]
  (let [actions {"exits" list-exits "doors" list-exits "open" take-exit}
        action-list (str/split action #" ")
        possible-actions (filter #(contains? actions %) action-list)
        ]

    (if (empty? possible-actions)
      "I don't know how to do that"
      ((actions (first possible-actions)) player-id action room-id)
      )
    )
  )

