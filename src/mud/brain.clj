(ns mud.brain
  (:require
    [mud.combat :as combat]
    [mud.exits :as exits]
    [mud.treasure :as treasure]
    [clojure.string :as str]
    [mud.core :as core]))


(defn verbs
  []
  {"exits" exits/list-exits "doors" exits/list-exits "look" exits/list-exits
   "open" exits/take-exit "fight" combat/fight
   "search" treasure/list-treasure-in-room "take" treasure/take-item-from-room
   "help" core/help
   }
  )


(defn action [player-id user-input room-id]
  (let [user-words (str/split user-input #"\s+")
        verb (first (filter #(contains? (verbs) %) user-words))]
    (if verb
      (((verbs) verb) player-id user-input room-id)
      "I don't know how to do that")))

