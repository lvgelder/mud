(ns mud.treasure-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.models :as models]
            [mud.treasure :refer :all]
            [mud.brain :refer :all]))

(fact "If a room has a treasure collect it"
      (get-treasure-from-room 1 "fish" 1) => irrelevant
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :treasure [{:id 42 :name "key"}]}
        (models/collect-treasure 1 42) => irrelevant :times 1
        )
      )

(fact "If a room has no treasure say that"
      (get-treasure-from-room 1 "fish" 1) => "There is no treasure in this room."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :treasure []}
        )
      )

(fact "Get back the treasure in the room"
      (list-treasure-in-room 1 "" 1) => "<p>You see 1 items in this room.</p> <ul><li>A shiny key</li></ul>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :treasure [{:id 43 :description "A shiny key"}]}
        )
      )

(fact "List all the treasure in the room"
      (list-treasure-in-room 1 "" 1) => "<p>You see 2 items in this room.</p> <ul><li>A shiny key</li><li>A newspaper</li></ul>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :treasure [{:id 43 :description "A shiny key"} {:id 44 :description "A newspaper"}]}
        )
      )

(fact "You can't search a room if there is a monster and it isn't dead yet"
      (list-treasure-in-room 1 "" 1) => "You try to search the room but the vampire tries to eat you..."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster [{:id 1 :name "vampire"}]
                                  :treasure [{:id 43 :description "A shiny key"}]}
        (models/monsters_killed 1 1) => []
        )
      )


(fact "Can take key if ask for it"
      (take-item-from-room 1 "take key" 1) => "You have the key."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [{:id 43 :name "key" :description "A shiny key"}]}
        (models/collect-treasure 1 43) => irrelevant :times 1
        )
      )


(fact "Can't take something if there is no treasure"
      (take-item-from-room 1 "take key" 1) => "You can't take that."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure []}
        )
      )

(fact "Can't take treasure if it doesn't match the name of the treasure"
      (take-item-from-room 1 "take fish" 1) => "You can't take that."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [{:id 43 :name "key" :description "A shiny key"}]}
        )
      )

(fact "Can't take treasure if there is a live monster in the room"
      (take-item-from-room 1 "take key" 1) => "You can't take it because the vampire tries to eat you."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster [{:id 1 :name "vampire"}]
                                  :treasure [{:id 43 :name "key" :description "A shiny key"}]}
        (models/monsters_killed 1 1) => []
        )
      )
