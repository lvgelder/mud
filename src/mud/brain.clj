(ns mud.brain
  (:require
    [mud.combat :as combat]
    [mud.exits :as exits]
    [mud.treasure :as treasure]
    [clojure.string :as str]
    [mud.core :as core]
    [mud.chat :as chat]))


(defn use-what [player-id user-input room-id]
  (if (exits/using-key? user-input)
    (exits/take-exit player-id user-input room-id)
    "I don't know how to use that."))

(defn try-what [player-id user-input room-id]
  (if (exits/using-key? user-input)
    (exits/take-exit player-id user-input room-id)
    "I don't know how to try that."))

(defn verbs
  []
  {"exits" exits/list-exits "doors" exits/list-exits "look" exits/list-exits
   "open" exits/take-exit "fight" combat/fight-what "use" use-what "try" try-what "unlock" exits/take-exit
   "search" treasure/search "take" treasure/take-what
   "help" core/help
   "say" chat/say
   "drop" treasure/drop-item "eat" treasure/eat "drink" treasure/drink
   "wear" treasure/wear "combine" treasure/combine-treasure
   })

(defn action [player-id user-input room-id]
  (let [user-words (str/split user-input #"\s+")
        verb (first (filter #(contains? (verbs) %) user-words))]
    (if verb
      (((verbs) verb) player-id user-input room-id)
      "I don't know how to do that")))



