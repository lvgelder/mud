(ns mud.brain
  (:require
    [mud.models :as models]
    [clojure.string :as str]
    ))


(defn list-exits [player-id room-id]
  (let [exits (models/exits-by-room room-id)]
    (apply str (concat (format "<p>You see %s exits: </p>" (count exits)) (map #(str (:description %)) exits)))
    )
  )

(defn action [player-id action room-id]
  (let [actions {"exits" list-exits "doors" list-exits}
        action-list (str/split action #" ")
        possible-actions (filter #(contains? actions %) action-list)
        ]

    (if (empty? possible-actions)
      "I don't know how to do that"
      ((actions (first possible-actions)) player-id room-id)
      )
    )
  )

