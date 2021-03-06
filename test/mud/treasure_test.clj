(ns mud.treasure-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.models :as models]
            [mud.treasure :refer :all]
            [mud.brain :refer :all]
            [mud.core :as core]
            [mud.chat :as chat]))

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
      (def player {:id 1 :treasure [] :monster [{:id 1 :name "vampire"}]})

      (search 1 "search vampire" 1) => irrelevant
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster monsters :treasure [{:id 1 :description "A shiny key"}]}
        (models/player-by-id 1) => player
        (list-treasure-from-monster player monsters) => irrelevant :times 1))

(fact "Searching the vampire should list its treasure"
      (def treasure [{:description "A cup of coffee"}{:description "A cookie"}])
      (def player {:id 1 :treasure []})

      (list-treasure-from-monster player [{:id 42 :name "vampire"}]) => "<p>You search the vampire and find 2 items.</p> <ul><li>A cup of coffee</li><li>A cookie</li></ul>"
      (provided
        (models/monster-by-id 42) => {:id 42 :name "vampire" :treasure treasure}
        )
      )

(fact "Searching the vampire should not list treasure taken from friend group"
      (def treasure [{:id 1 :description "A cup of coffee"}{:id 2 :description "A cookie"}{:id 3 :description "cupcake"}])
      (def player {:id 1 :treasure [] :friend_group [{:id 1}]})

      (list-treasure-from-monster player [{:id 42 :name "vampire"}]) => "<p>You search the vampire and find 2 items.</p> <ul><li>A cup of coffee</li><li>A cookie</li></ul>"
      (provided
        (models/monster-by-id 42) => {:id 42 :name "vampire" :treasure treasure}
        (models/friend-group-by-id 1) => {:id 1 :treasure [{:id 3 :description "cupcake"}]}))


(fact "Searching the vampire should 0 items if it has no treasure."
      (def player {:id 1 :treasure []})

      (list-treasure-from-monster player [{:id 42 :name "vampire"}]) => "<p>You search the vampire and find 0 items.</p> <ul></ul>"
      (provided
        (models/monster-by-id 42) => {:id 42 :name "vampire" :treasure []}
        )
      )

(fact "Searching the vampire should 0 items if you have eaten the items"
      (def treasure [{:id 42 :name "soup"}])
      (def player {:id 1 :treasure []})

      (list-treasure-from-monster player [{:id 42 :name "vampire"}]) => "<p>You search the vampire and find 0 items.</p> <ul></ul>"
      (provided
        (models/monster-by-id 42) => {:id 42 :name "vampire" :treasure treasure}
        (models/eaten-treasure-by-player-id 1) => treasure
        )
      )

(fact "Searching the vampire should 0 items if you have already taken the items"
      (def treasure [{:id 42 :name "soup"}])
      (def player {:id 1 :treasure treasure})

      (list-treasure-from-monster player [{:id 42 :name "vampire"}]) => "<p>You search the vampire and find 0 items.</p> <ul></ul>"
      (provided
        (models/monster-by-id 42) => {:id 42 :name "vampire" :treasure treasure}
        (models/eaten-treasure-by-player-id 1) => []
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

(fact "List all the treasure in the room that you don't already have or have eaten"
      (list-treasure-in-room 1 "" 1) => "<p>You see 1 items in this room.</p> <ul><li>A shiny key</li></ul>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster []
                                  :treasure [{:id 43 :description "A shiny key"}{:id 44 :description "A newspaper"}{:id 56 :name "cupcake"}]}
        (models/player-by-id 1) => {:id 1 :treasure [{:id 44 :description "A newspaper"}] :monster []}
        (models/eaten-treasure-by-player-id 1) => [{:id 56 :name "cupcake"}]
        )
      )

(fact "List all the treasure in the room that your friend group doesn't already have"
      (list-treasure-in-room 1 "" 1) => "<p>You see 1 items in this room.</p> <ul><li>A shiny key</li></ul>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room"
                                  :monster []
                                  :treasure [{:id 43 :description "A shiny key"}{:id 44 :description "A newspaper"}{:id 56 :name "cupcake"}]}
        (models/player-by-id 1) => {:id 1 :treasure [{:id 44 :description "A newspaper"}] :monster [] :friend_group [{:id 1}]}
        (models/eaten-treasure-by-player-id 1) => []
        (models/friend-group-by-id 1) => {:id 1 :treasure [{:id 56 :name "cupcake"}]}))

(fact "Can take key if ask for it"
      (def player {:id 1 :treasure [] :monster []})
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (take-item-from-room 1 "take key" 1) => "<p>You have the key.</p>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [treasure]}
        (models/player-by-id 1) => player
        (models/collect-treasure 1 43) => irrelevant :times 1
        )
      )

(fact "If you have a friend group, mark treasure as taken"
      (def player {:id 1 :treasure [] :monster [] :friend_group [{:id 1}]})
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (take-item-from-room 1 "take key" 1) => "<p>You have the key.</p>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [treasure]}
        (models/player-by-id 1) => player
        (models/friend-group-by-id 1) => {:id 1 :treasure []}
        (models/collect-treasure 1 43) => irrelevant :times 1
        (models/collect-treasure-for-friend-group 1 43) => irrelevant :times 1
        (chat/notify-taken-treasure player 1 treasure) => irrelevant :times 1))


(fact "Can't take something if there is no treasure"
      (take-item-from-room 1 "take key" 1) => "<p>You can't take that.</p>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure []}
        )
      )

(fact "Can't take treasure if it doesn't match the name of the treasure"
      (take-item-from-room 1 "take fish" 1) => "<p>You can't take that.</p>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [{:id 43 :name "key" :description "A shiny key"}]}
        )
      )

(fact "Can't take key if already have it"
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (def player {:id 1 :treasure [treasure] :monster []})

      (take-item-from-room 1 "take key" 1) => "<p>You already have that.</p>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [treasure]}
        (models/player-by-id 1) => player
        (models/collect-treasure 1 43) => irrelevant :times 0
        )
      )

(fact "can't take treasure if you already have 5 items"
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (def player {:id 101 :treasure [{:id 1}{:id 2}{:id 3}{:id 4}{:id 5}] :monster []})

      (take-item-from-room 101 "take key" 1) => "<p>You already have 5 items. You need to drop something.</p>"
      (provided
        (models/room-by-id 1) => {:id 1 :description "A room" :monster []
                                  :treasure [treasure]}
        (models/player-by-id 101) => player
        (models/collect-treasure 101 43) => irrelevant :times 0
        )
      )

(fact "can drop treasure you possess"
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (def player {:id 1 :treasure [treasure] :monster []})

      (drop-item 1 "drop key" 1) => "<p>You put the key down.</p>"
      (provided
        (models/player-by-id 1) => player
        (models/remove-treasure-from-player 1 43) => irrelevant :times 1
        )
      )

(fact "can't drop treasure you don't possess"
      (def treasure {:id 43 :name "key" :description "A shiny key"})
      (def player {:id 1 :treasure [treasure] :monster []})

      (drop-item 1 "drop fish" 1) => "<p>You don't have that.</p>"
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

(fact "if monster dead and room mentioned pass on to take item from room"
      (take-what 1 "take key from room" 1) => irrelevant
      (provided
        (models/player-by-id 1) => {:id 1 :monster [{:id 1 :name "vampire"}]}
        (models/room-by-id 1) => {:id 1 :monster [{:id 1 :name "vampire"}]}
        (take-item-from-room 1 "take key from room" 1) => irrelevant :times 1
        )
      )

(fact "if monster dead and monster mentioned pass on to take item from monster"
      (def player {:id 1 :monster [{:id 1 :name "vampire"}]})
      (def monsters [{:id 1 :name "vampire"}])

      (take-what 1 "take key from vampire" 1) => irrelevant
      (provided
        (models/player-by-id 1) => player
        (models/room-by-id 1) => {:id 1 :monster monsters}
        (take-item-from-monster player "take key from vampire" 1 monsters) => irrelevant :times 1
        )
      )

(fact "Can't take something from monster if there is no treasure"
      (def player {:id 1})
      (def monsters-mentioned [{:id 48 :name "vampire"}])

      (take-item-from-monster player "take key from vampire" 1 monsters-mentioned) => "<p>You can't take that.</p>"
      (provided
        (models/monster-by-id 48) => {:id 1 :name "vampire" :treasure []}))

(fact "Can take something from monster if there is treasure"
      (def player {:id 1 :treasure []})
      (def monsters-mentioned [{:id 48 :name "vampire"}])

      (take-item-from-monster player "take cupcake from vampire" 1 monsters-mentioned) => "<p>You have the cupcake.</p>"
      (provided
        (models/monster-by-id 48) => {:id 1 :name "vampire" :treasure [{:id 3 :name "cupcake"}]}
        (models/collect-treasure 1 3) => irrelevant :times 1))

(fact "if monster dead and say take from but not name of monster say you can't take that"
      (take-what 1 "take key from fish" 1) => "You can't take that."
      (provided
        (models/player-by-id 1) => {:id 1 :monster [{:id 1 :name "vampire"}]}
        (models/room-by-id 1) => {:id 1 :monster [{:id 1 :name "vampire"}]}
        (take-item-from-room 1 "take key" 1) => irrelevant :times 0
        )
      )

(fact "can't eat something you don't have"
      (eat 1 "eat fish" 1) => "I don't know what that is."
      (provided
        (models/player-by-id 1) => {:id 1 :treasure []}
        )
      )

(fact "can't eat something non-edible"
      (eat 1 "eat key" 1) => "You can't eat that."
      (provided
        (models/player-by-id 1) => {:id 1 :treasure [{:id 1 :name "key"}]}
        )
      )

(fact "can eat edible treasure - doesn't set hit points if you haven't lost any"
      (def treasure [{:id 41 :name "cupcake" :type "edible" :hit_points 5 :action_description "The icing is over-sweet."}])

      (eat 1 "eat cupcake" 1) => "You eat the cupcake. The icing is over-sweet."
      (provided
        (models/player-by-id 1) => {:id 1 :hit_points 5 :max_hit_points 5 :treasure treasure}
        (models/remove-treasure-from-player 1 41) => irrelevant :times 1
        (models/eat-treasure 1 41) => irrelevant :times 1
        )
      )

(fact "edible treasure restores hit points if you have lost some"
      (def treasure [{:id 41 :name "cupcake" :type "edible" :hit_points 5 :action_description "The icing is over-sweet."}])

      (eat 1 "eat cupcake" 1) => "You eat the cupcake. The icing is over-sweet."
      (provided
        (models/player-by-id 1) => {:id 1 :hit_points 1 :max_hit_points 5 :treasure treasure}
        (models/remove-treasure-from-player 1 41) => irrelevant :times 1
        (models/eat-treasure 1 41) => irrelevant :times 1
        (models/set-hit-points 1 5) => irrelevant :times 1))

(fact "can't drink something you don't have"
      (drink 1 "drink fish" 1) => "I don't know what that is."
      (provided
        (models/player-by-id 1) => {:id 1 :treasure []}
        )
      )

(fact "can't drink something non-drinkable"
      (drink 1 "drink key" 1) => "You can't drink that."
      (provided
        (models/player-by-id 1) => {:id 1 :treasure [{:id 1 :name "key"}]}
        )
      )

(fact "can drink drinkable treasure"
      (def treasure [{:id 41 :name "coffee" :type "drinkable" :action_description "Coffee is always fantastic." :hit_points 5}])

      (drink 1 "drink coffee" 1) => "You drink the coffee. Coffee is always fantastic."
      (provided
        (models/player-by-id 1) => {:id 1 :hit_points 5 :max_hit_points 5 :treasure treasure}
        (models/remove-treasure-from-player 1 41) => irrelevant :times 1
        (models/eat-treasure 1 41) => irrelevant :times 1
        )
      )

(fact "can't wear something you don't have"
      (wear 1 "wear fish" 1) => "I don't know what that is."
      (provided
        (models/player-by-id 1) => {:id 1 :treasure []}
        )
      )

(fact "can't wear something you are already wearing"
      (wear 1 "wear hat" 1) => "You are already wearing that."
      (provided
        (models/player-by-id 1) => {:id 1 :treasure [{:id 1 :name "hat" :type "wearable"}]}
        (models/worn-treasure-by-player-id 1) => [{:id 1 :name "hat"}]
        )
      )

(fact "can't wear something not wearable"
      (wear 1 "wear fish" 1) => "You can't wear that."
      (provided
        (models/player-by-id 1) => {:id 1 :treasure [{:id 1 :name "fish" :type "wet"}]}
        )
      )

(fact "restore hit points does nothing if you are at full hitpoints"
      (def treasure {:id 41 :name "cupcake" :type "edible" :hit_points 5 :action_description "The icing is over-sweet."})
      (def player {:id 1 :max_hit_points 5 :hit_points 5})

      (restore-hit-points player treasure) => irrelevant
      (provided
        (models/set-hit-points 1 5) => irrelevant :times 0))

(fact "restore hit points sets you to max hit points if that is less than the treasure gives you"
      (def treasure {:id 41 :name "cupcake" :type "edible" :hit_points 50 :action_description "The icing is over-sweet."})
      (def player {:id 1 :max_hit_points 5 :hit_points 1})

      (restore-hit-points player treasure) => irrelevant
      (provided
        (models/set-hit-points 1 5) => irrelevant :times 1))

(fact "restore hit points adds treasure hit points if that is less than max hit points"
      (def treasure {:id 41 :name "cupcake" :type "edible" :hit_points 3 :action_description "The icing is over-sweet."})
      (def player {:id 1 :max_hit_points 5 :hit_points 1})

      (restore-hit-points player treasure) => irrelevant
      (provided
        (models/set-hit-points 1 4) => irrelevant :times 1))


(fact "combinable treasure is not combinable if player doesn't have it"
      (def player {:id 1 :treasure []} )

      (combine-treasure 1 "combine hat and shoe" 1) => "You don't have that."
      (provided
        (models/player-by-id 1) => player
        )
      )

(fact "combinable treasure is not combinable if treasure not combinable"
             (def treasure1 {:id 1 :type "combinbable" :name "pen"})
             (def treasure2 {:id 2 :type "edible" :name "fish"})
             (def player {:id 1 :treasure [treasure1 treasure2]} )

             (combine-treasure 1 "combine pen and fish" 1) => "You can't combine that."
             (provided
               (models/player-by-id 1) => player
               )
             )

(fact "combinable treasure is not combinable if treasure not part of same combinable treasure"
      (def treasure1 {:id 1 :type "combinable" :name "pen"})
      (def treasure2 {:id 2 :type "combinable" :name "fish"})
      (def treasure3 {:id 3 :type "combinable" :name "ink"})
      (def player {:id 1 :treasure [treasure1 treasure2]} )

      (combine-treasure 1 "combine pen and fish" 1) => "You can't combine that."
      (provided
        (models/player-by-id 1) => player
        (models/find-combinable-items 1) => [treasure1 treasure3]
        )
      )

(fact "combinable treasure is combinable if treasure part of same combinable treasure"
      (def treasure1 {:id 1 :type "combinable" :name "pen"})
      (def treasure2 {:id 2 :type "combinable" :name "ink"})
      (def treasure3 {:id 3 :type "combinable" :name "pen with ink" :action_description "You add ink to the pen."})
      (def player {:id 1 :treasure [treasure1 treasure2]} )

      (combine-treasure 1 "combine pen and ink" 1) => "You add ink to the pen."
      (provided
        (models/player-by-id 1) => player
        (models/find-combinable-items 1) => [treasure1 treasure2]
        (models/find-combined-treasure 1) => [treasure3]
        (models/remove-treasure-from-player 1 1) => :irrelevant :times 1
        (models/remove-treasure-from-player 1 2) => :irrelevant :times 1
        (models/eat-treasure 1 1) => :irrelevant :times 1
        (models/eat-treasure 1 2) => :irrelevant :times 1
        (models/collect-treasure 1 3) => irrelevant :times 1))