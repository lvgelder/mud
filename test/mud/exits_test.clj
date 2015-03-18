(ns mud.exits-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.models :as models]
            [mud.exits :refer :all]
            [mud.brain :refer :all]
            [mud.chat :as chat]))

(fact "If a room has one exit, and no monsters list it and its description"
      (list-exits 1 "exits" 1) => "<p>You see 1 exits:</p> <ul><li>A madeup door</li></ul>"
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door"}]
        (models/monster-by-room 1) => []
        )
      )

(fact "If a room has no exits, and no monsters list 0 exits"
      (list-exits 1 "exits" 1) => "<p>You see 0 exits:</p> <ul></ul>"
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

(fact "if there is one door and you match its keyword return that"
      (def player {:id 1})
      (def exit1 {:id 1 :to_room 42 :from_room 1 :keywords "door" :locked 0})

      (take-exit 1 "open door" 1) => irrelevant
      (provided
        (models/player-by-id 1) => player
        (models/exits-by-room 1) => [exit1]
        (models/monster-by-room 1) => []
        (models/set-player-room 1 42) => irrelevant :times 1
        (chat/join-room player 42) => irrelevant :times 1))

(fact "if there is one door and you match one of its keyword return that"
      (def player {:id 1})
      (def exit1 {:id 1 :to_room 42 :from_room 1 :keywords "door exit" :locked 0})

      (take-exit 1 "open door" 1) => "You open the door. "
      (provided
        (models/player-by-id 1) => player
        (models/exits-by-room 1) => [exit1]
        (models/monster-by-room 1) => []
        (chat/join-room player 42) => irrelevant :times 1
        (models/set-player-room 1 42) => irrelevant :times 1))

(fact "if you don't match the keywords return nothing"
      (def player {:id 1})
      (def exit1 {:id 1 :to_room 42 :from_room 1 :keywords "fish" :locked 0})

      (take-exit 1 "open door" 1) => "I don't know which door to open."
      (provided
        (models/player-by-id 1) => player
        (models/exits-by-room 1) => [exit1]
        (models/monster-by-room 1) => []
        (models/set-player-room 1 42) => irrelevant :times 0
        (chat/join-room player 42) => irrelevant :times 0))

(fact "if there is more than one exit that matches say you don't know which door to open"
      (def player {:id 1})
      (def exit1 {:id 1 :keywords "fish"})
      (def exit2 {:id 2 :keywords "pants"})
      (take-exit 1"open fish pants door" 1) => "I don't know which door to open."

      (provided
        (models/player-by-id 1) => player
        (models/exits-by-room 1) => [exit1 exit2]
        (models/monster-by-room 1) => []
        (models/set-player-room 1 42) => irrelevant :times 0))

(fact "Take exit doesn't work if there is no exit"
      (take-exit 1 "take exit 1" 1) => "There is no door."
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/exits-by-room 1) => []
        (models/monster-by-room 1) => []
        (models/set-player-room 1 nil) => irrelevant :times 0))

(fact "Take exit doesn't work if room is locked"
      (take-exit 1 "take east exit" 1) => "This door is locked. You can't open it."
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 1 :keywords "east"}]
        (models/monster-by-room 1) => []))

(fact "Take exit doesn't work if there is a non-killed monster"
      (take-exit 1 "take east exit" 1) => "You can't reach the door because there is a vampire trying to eat you."
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 0 :keywords "east"}]
        (models/monster-by-room 1) => [{:id 1 :name "vampire"}]))

(fact "Take exit unlocks door if player has a key and uses it on the correct door"
      (def player {:id 1 :monster [] :treasure [{:id 4 :name "key"}]})

      (take-exit 1 "use key on east exit" 1) => "You unlock the door and move to the next room."
      (provided
        (models/player-by-id 1) => player
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 1 :keywords "east"}]
        (models/monster-by-room 1) => []
        (models/remove-treasure-from-player 1 4) => irrelevant :times 1
        (models/set-player-room 1 42) => irrelevant :times 1
        (chat/join-room player 42) => irrelevant :times 1))

(fact "Take exit doesnt unlock door if not used on correct door"
      (take-exit 1 "use key" 1) => "I don't know which door to open."
      (provided
        (models/player-by-id 1) => {:id 1 :monster [] :treasure [{:id 4 :name "key"}]}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 1 :keywords "east"}]
        (models/monster-by-room 1) => []
        (models/remove-treasure-from-player 1 4) => irrelevant :times 0
        (models/set-player-room 1 42) => irrelevant :times 0))

(fact "Take exit doesn't unlock door if player has a key but doesn't use it"
      (take-exit 1 "open east door" 1) => "This door is locked. You can't open it."
      (provided
        (models/player-by-id 1) => {:id 1 :monster [] :treasure [{:id 42 :name "key"}]}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 1 :keywords "east"}]
        (models/monster-by-room 1) => []
        (models/set-player-room 1 42) => irrelevant :times 0))

(fact "Use key doesnt do anything if you dont have a key"
      (take-exit 1 "use key on east door" 1) => "You don't have the key."
      (provided
        (models/player-by-id 1) => {:id 1 :monster [] :treasure [{:id 4 :name "fish"}]}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 1 :keywords "east"}]
        (models/monster-by-room 1) => []
        (models/remove-treasure-from-player 1 4) => irrelevant :times 0
        (models/set-player-room 1 42) => irrelevant :times 0))

(fact "Use key doesnt do anything if room is not locked"
      (take-exit 1 "use key on east door" 1) => "The door is not locked."
      (provided
        (models/player-by-id 1) => {:id 1 :monster [] :treasure [{:id 4 :name "key"}]}
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door" :to_room 42 :from_room 1 :locked 0 :keywords "east"}]
        (models/monster-by-room 1) => []
        (models/remove-treasure-from-player 1 4) => irrelevant :times 0
        (models/set-player-room 1 42) => irrelevant :times 0))

(fact "Using key counts as using key"
      (using-key? "use key") => true)

(fact "Unlocking door counts as using key"
      (using-key? "unlock door") => true)

(fact "Trying key counts as using key"
      (using-key? "try key on door") => true)

(fact "Can't unlock a fish"
      (using-key? "unlock fish") => nil)

(fact "Can't use a fish"
      (using-key? "try fish on door") => nil)

(fact "Can't open door it is locked"
      (using-key? "open door") => false)


