(ns mud.treasure-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.models :as models]
            [mud.treasure :refer :all]
            [mud.brain :refer :all]
            [mud.core :as core]))

(fact "Can't search if there is an unkilled monster in the room"
      (search 1 "search" 1) => "You try to search but the vampire tries to eat you..."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster [{:id 1 :name "vampire"}]
                                  :treasure []}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster []}
        )
      )

(fact "Can search if there is a killed monster in the room"
      (search 1 "search" 1) => irrelevant
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster [{:id 1 :name "vampire"}]
                                  :treasure []}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster [{:id 1 :name "vampire"}]}
        (list-treasure-in-room 1 "search" 1) => irrelevant :times 1))

(fact "Typing 'search' by itself lists the treasure in the room"
      (search 1 "search" 1) => irrelevant
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster [] :treasure [{:id 1 :description "A shiny key"}]}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster []}
        (list-treasure-in-room 1 "search" 1) => irrelevant :times 1))

(fact "Typing 'search fish' tells you that you can't do that"
      (search 1 "search fish" 1) => "You can't search the fish because there is no fish."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster [] :treasure [{:id 1 :description "A shiny key"}]}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster []}
        (list-treasure-in-room 1 "search fish" 1) => irrelevant :times 0))

(fact "Typing 'search room' should search the room you are in"
      (search 1 "search room" 1) => irrelevant
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster [] :treasure [{:id 1 :description "A shiny key"}]}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster []}
        (list-treasure-in-room 1 "search room" 1) => irrelevant :times 1))

(fact "Typing 'search vampire' should search the vampire"
      (def monsters [{:id 1 :name "vampire"}])

      (search 1 "search vampire" 1) => irrelevant
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster monsters :treasure [{:id 1 :description "A shiny key"}]}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster [{:id 1 :name "vampire"}]}
        (list-treasure-from-monster monsters) => irrelevant :times 1))

(fact "Searching the vampire should list its treasure"
      (def treasure [{:description "A cup of coffee"}{:description "A cookie"}])
      (list-treasure-from-monster [{:id 42 :name "vampire"}]) => "<p>You search the vampire and find 2 items.</p> <ul><li>A cup of coffee</li><li>A cookie</li></ul>"
      (provided
        (models/monster-by-id 42) => {:id 42 :name "vampire" :treasure treasure}
        )
      )

(fact "Searching the vampire should 0 items if it has no treasure."
      (list-treasure-from-monster [{:id 42 :name "vampire"}]) => "<p>You search the vampire and find 0 items.</p> <ul></ul>"
      (provided
        (models/monster-by-id 42) => {:id 42 :name "vampire" :treasure []}
        )
      )


(fact "List all the treasure in the room"
      (list-treasure-in-room 1 "" 1) => "<p>You see 2 items in this room.</p> <ul><li>A shiny key</li><li>A newspaper</li></ul>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster []
                                  :treasure [{:id 43 :description "A shiny key"} {:id 44 :description "A newspaper"}]}
        (models/player-by-id 1) => {:id 1 :treasure [] :monster []}
        )
      )

(fact "List all the treasure in the room that you don't already have"
      (list-treasure-in-room 1 "" 1) => "<p>You see 1 items in this room.</p> <ul><li>A shiny key</li></ul>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster []
                                  :treasure [{:id 43 :description "A shiny key"}{:id 44 :description "A newspaper"}]}
        (models/player-by-id 1) => {:id 1 :treasure [{:id 44 :description "A newspaper"}] :monster []}
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

(fact "Can't take key if already have it"
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (def player {:id 1 :treasure [treasure] :monster []})

      (take-item-from-room 1 "take key" 1) => "You already have that."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [treasure]}
        (models/player-by-id 1) => player
        (models/collect-treasure 1 43) => irrelevant :times 0
        )
      )

(fact "can't take treasure if you already have 5 items"
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (def player {:id 1 :treasure [{:id 1}{:id 2}{:id 3}{:id 4}{:id 5}] :monster []})

      (take-item-from-room 1 "take key" 1) => "You already have 5 items. You need to drop something."
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [treasure]}
        (models/player-by-id 1) => player
        (models/collect-treasure 1 43) => irrelevant :times 0
        )
      )

(fact "can drop treasure you possess"
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (def player {:id 1 :treasure [treasure] :monster []})

      (drop-item 1 "drop key" 1) => "You put the key down."
      (provided
        (models/player-by-id 1) => player
        (models/remove-treasure-from-player 1 43) => irrelevant :times 1
        )
      )

(fact "can't drop treasure you don't possess"
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (def player {:id 1 :treasure [treasure] :monster []})

      (drop-item 1 "drop fish" 1) => "You don't have that."
      (provided
        (models/player-by-id 1) => player
        (models/remove-treasure-from-player 1 43) => irrelevant :times 0
        )
      )

(fact "can't take anything if monster isn't dead"
      (take-what 1 "take key" 1) => "You can't take that because the vampire tries to eat you."
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/room-by-id 1) => {:id 1 :monster [{:id 1 :name "vampire"}]})
        )

(fact "if monster dead and monster not mentioned pass on to take item"
      (take-what 1 "take key" 1) => irrelevant
      (provided
        (models/player-by-id 1) => {:id 1 :monster [{:id 1 :name "vampire"}]}
        (models/room-by-id 1) => {:id 1 :monster [{:id 1 :name "vampire"}]}
        (take-item-from-room 1 "take key" 1) => irrelevant :times 1
        )
      )

(fact "if monster dead and monster mentioned pass on to take item from monster"
      (def player {:id 1 :monster [{:id 1 :name "vampire"}]})
      (def monsters [{:id 1 :name "vampire"}])

      (take-what 1 "take key from vampire" 1) => irrelevant
      (provided
        (models/player-by-id 1) => player
        (models/room-by-id 1) => {:id 1 :monster monsters}
        (take-item-from-monster player "take key from vampire" monsters) => irrelevant :times 1
        )
      )

(fact "Can't take something from monster if there is no treasure"
      (def player {:id 1})
      (def monsters-mentioned [{:id 48 :name "vampire"}])

      (take-item-from-monster player "take key from monster" monsters-mentioned) => "You can't take that."
      (provided
        (models/monster-by-id 48) => {:id 1 :name "vampire" :treasure []}))