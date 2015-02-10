(ns mud.exits-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.models :as models]
            [mud.exits :refer :all]
            [mud.brain :refer :all]))

(fact "If a room has one exit, and no monsters list it and its description"
      (list-exits 1 "exits" 1) => "<p>You see 1 exits:</p> A madeup door"
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door"}]
        (models/monster-by-room 1) => []
        )
      )

(fact "If a room has no exits, and no monsters list 0 exits"
      (list-exits 1 "exits" 1) => "<p>You see 0 exits:</p> "
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/exits-by-room 1) => []
        (models/monster-by-room 1) => []
        )
      )

(fact "If a room has one exit, and a non-killed monster say you can't search because of the monster"
      (list-exits 1 "exits" 1) => "You can't tell if there is a door because there is a vampire trying to eat you."
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door"}]
        (models/monster-by-room 1) => [{:id 1 :name "vampire"}]
        )
      )

(fact "Take exit sets players new room to be from room from the exit"
      (take-exit 1 "take exit 1" 1) => irrelevant
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 0}]
        (models/monster-by-room 1) => []
        (models/set-player-room 1 42) => irrelevant :times 1
        )
      )

(fact "Take exit doesn't work if room is locked"
      (take-exit 1 "take exit 1" 1) => "This door is locked. You can't open it."
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 1}]
        (models/monster-by-room 1) => []
        )
      )

(fact "Take exit doesn't work if there is a non-killed monster"
      (take-exit 1 "take exit 1" 1) => "You can't reach the door because there is a vampire trying to eat you."
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 1}]
        (models/monster-by-room 1) => [{:id 1 :name "vampire"}]
        )
      )