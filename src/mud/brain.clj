(ns mud.brain
  (:require
    [mud.combat :as combat]
    [mud.exits :as exits]
    [mud.treasure :as treasure]
    [clojure.string :as str]
    ))


(defn actions
  []
  {"exits" exits/list-exits "doors" exits/list-exits "look" exits/list-exits
   "open" exits/take-exit "fight" combat/fight
   "search" treasure/list-treasure-in-room "take" treasure/take-item-from-room}
  )


(defn action [player-id action room-id]
  (let [action-list (str/split action #" ")
        verb (first (filter #(contains? (actions) %) action-list))]
    (if verb
      (((actions) verb) player-id action room-id)
      "I don't know how to do that")))

