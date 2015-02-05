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
      (take-exit 1 "take exit 1" 1) => irrelevant
      (provided
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 0}]
        (models/set-player-room 1 42) => irrelevant :times 1
        )
      )

(fact "Take exit doesn't work if room is locked"
      (take-exit 1 "take exit 1" 1) => "This door is locked. You can't open it."
      (provided
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 1}]
        )
      )

(fact "Asking to do something it doesnt understand should return that"
      (action 1 "madeup string of foo" 1) => "I don't know how to do that"
      )

(fact "Asking for exits should call list-exits"
      (action 1 "exits" 1) => irrelevant
      (provided
        (list-exits 1 "exits" 1) => irrelevant :times 1
        )
      )

(fact "Asking for open door should call take exit"
      (action 1 "open door" 1) => irrelevant
      (provided
        (take-exit 1 "open door" 1) => irrelevant :times 1
        )
      )