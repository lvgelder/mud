(ns mud.brain
  (:require
    [mud.models :as models]
    ))


(defn list-exits [player-id room-id]
  (let [exits (models/exits-by-room room-id)]
    (apply str (concat (format "<p>You see %s exits: </p>" (count exits)) (map #(str (:description %)) exits)))
    )
  )

(defn action [player-id action room-id]
  (def actions {"exits" list-exits})
  ((actions "exits") player-id room-id)
  )

