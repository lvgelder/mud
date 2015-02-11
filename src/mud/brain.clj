(ns mud.brain
  (:require
    [mud.combat :as combat]
    [mud.exits :as exits]
    [mud.treasure :as treasure]
    [clojure.string :as str]
    [mud.core :as core]))


; only one thing to use right now. this will need to get smarter later
(defn use-what [player-id user-input room-id]
  (exits/take-exit player-id user-input room-id)
  )

; only one thing to try right now. this will need to get smarter later
(defn try-what [player-id user-input room-id]
  (exits/take-exit player-id user-input room-id)
  )

(defn verbs
  []
  {"exits" exits/list-exits "doors" exits/list-exits "look" exits/list-exits
   "open" exits/take-exit "fight" combat/fight "use" use-what "try" try-what "unlock" exits/take-exit
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





