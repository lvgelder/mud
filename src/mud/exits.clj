(ns mud.exits
  (:require
    [mud.models :as models]
    ))

(defn list-exits [_ _ room-id]
  (let [exits (models/exits-by-room room-id)]
    (apply str (concat (format "<p>You see %s exits:</p> " (count exits)) (map #(str (:description %)) exits)))
    )
  )

(defn take-exit [player-id action room-id]
  (let [exits (models/exits-by-room room-id)]
    (if (= (:locked (first exits)) 1)
      "This door is locked. You can't open it."
      (models/set-player-room player-id (:to_room (first exits)))
      )

    )
  )