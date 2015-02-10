(ns mud.treasure-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.models :as models]
            [mud.treasure :refer :all]
            [mud.brain :refer :all]
            [mud.core :as core]))

(fact "Get back the treasure in the room if there are no monsters"
      (list-treasure-in-room 1 "" 1) => "<p>You see 1 items in this room.</p> <ul><li>A shiny key</li></ul>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster [] :treasure [{:id 43 :description "A shiny key"}]}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster []}
        )
      )

(fact "List all the treasure in the room if there are no monsters"
      (list-treasure-in-room 1 "" 1) => "<p>You see 2 items in this room.</p> <ul><li>A shiny key</li><li>A newspaper</li></ul>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster []
                                  :treasure [{:id 43 :description "A shiny key"} {:id 44 :description "A newspaper"}]}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster []}
        )
      )

(fact "You can't search a room if there is a monster and it isn't dead yet"
      (list-treasure-in-room 1 "" 1) => "You try to search the room but the vampire tries to eat you..."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster [{:id 1 :name "vampire"}]
                                  :treasure [{:id 43 :description "A shiny key"}]}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster []}
        )
      )

(fact "List all the treasure in the room if there are monsters but you already killed them"
      (list-treasure-in-room 1 "" 1) => "<p>You see 2 items in this room.</p> <ul><li>A shiny key</li><li>A newspaper</li></ul>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster [{:id 1 :name "vampire"}]
                                  :treasure [{:id 43 :description "A shiny key"} {:id 44 :description "A newspaper"}]}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster [{:id 1 :name "vampire"}]}
        )
      )


(fact "Can take key if ask for it"
      (def player {:id 1 :treasure [] :monster []})
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (take-item-from-room 1 "take key" 1) => "You have the key."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [treasure]}
        (models/player-by-id 1) => player
        (core/monsters-left-to-kill? player []) => false
        (core/already-taken-treasure player treasure) => false
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
      (def monster {:id 1 :name "vampire"})
      (def player {:id 1 :treasure [] :monster []})
      (def treasure {:id 43 :name "key" :description "A shiny key"})

      (take-item-from-room 1 "take key" 1) => "You can't take it because the vampire tries to eat you."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster [monster]
                                  :treasure [treasure]}
        (models/player-by-id 1) => player
        (core/monsters-left-to-kill? player [monster]) => true
        (models/collect-treasure 1 43) => irrelevant :times 0
        )
      )

(fact "Can't take key if already have it"
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (def player {:id 1 :treasure [treasure] :monster []})

      (take-item-from-room 1 "take key" 1) => "You already have that."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [treasure]}
        (models/player-by-id 1) => player
        (core/monsters-left-to-kill? player []) => false
        (core/already-taken-treasure player treasure) => true
        (models/collect-treasure 1 43) => irrelevant :times 0
        )
      )
