(ns mud.brain-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.models :as models]
            [mud.brain :refer :all]))

(fact "If a room has one exit, list it and its description"
      (list-exits 1 "exits" 1) => "<p>You see 1 exits:</p> A madeup door"
      (provided
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door"}]
        )
      )

(fact "Take exit sets players new room to be from room from the exit"
      (take-exit  1 "take exit 1" 1) => irrelevant
      (provided
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1}]
        (models/set-player-room 1 42) => irrelevant :times 1
        )
      )
