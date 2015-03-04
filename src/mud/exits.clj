(ns mud.exits
  (:require
    [mud.models :as models]
    [mud.core :as core]
    [clojure.string :as str]))

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

(defn using-key? [user-input]

  (def key-verbs {"use" "key" "try" "key" "unlock" "door"})

  (let [user-words (str/split user-input #"\s+")
        key-verb (first (filter #(contains? key-verbs %) user-words))]
    (if key-verb
      (core/seq-contains? user-words (key-verbs key-verb))
      false
      )
    )
  )

(defn locked-exit [player action exit]
  (if (using-key? action)
    (let [key (first (core/items-with-name (:treasure player) "key"))]
      (if (empty? key)
        "You don't have the key."
        (
          do
          (models/remove-treasure-from-player (:id player) (:id key))
          (models/set-player-room (:id player) (:to_room exit))
          "You unlock the door and move to the next room."
          )

        )
      )
    "This door is locked. You can't open it."
    )

  )

(defn take-exit [player-id action room-id]
  (let [player (models/player-by-id player-id)
        exit (first (models/exits-by-room room-id))
        monsters (models/monster-by-room room-id)
        ]
    (cond
      (core/monsters-left-to-kill? player monsters) (format "You can't reach the door because there is a %s trying to eat you." (:name (first monsters)))
      (empty? exit) "There is no door."
      (= (:locked exit) 1) (locked-exit player action exit)
      (using-key? action) "The door is not locked."
      :else (models/set-player-room player-id (:to_room exit))
      )
    )
  )