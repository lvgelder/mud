(ns mud.core-test
      (:require [clojure.test :refer :all]
                [midje.sweet :refer :all]
                [mud.core :refer :all]
                [mud.models :as models]))

(fact "return only monsters who are not killed"
      (def player {:id 3 :monster [{:id 42 :name "vampire"}]})
      (def monsters [{:id 42 :name "vampire"} {:id 43 :name "werewolf"}])

      (monsters-left-to-kill player monsters) => [{:id 43 :name "werewolf"}]
      )

(fact "return nothing if all monsters killed"
      (def player {:id 3 :monster [{:id 42 :name "vampire"}]})
      (def monsters [{:id 42 :name "vampire"} ])

      (monsters-left-to-kill player monsters) => []
      )

(fact "return nothing if no monsters"
      (def player {:id 3 :monster [{:id 42 :name "vampire"}]})
      (def monsters [])

      (monsters-left-to-kill player monsters) => []
      )

(fact "return true if there are monsters who haven't been killed"
      (def player {:id 3 :monster [{:id 42 :name "vampire"}]})
      (def monsters [{:id 42 :name "vampire"} {:id 43 :name "werewolf"}])

      (monsters-left-to-kill? player monsters) => true
      )

(fact "return false if all monsters have been killed"
      (def player {:id 3 :monster [{:id 42 :name "vampire"}]})
      (def monsters [{:id 42 :name "vampire"}])

      (monsters-left-to-kill? player monsters) => false
      )

(fact "return false if there are no monsters"
      (def player {:id 3 :monster []})
      (def monsters [])

      (monsters-left-to-kill? player monsters) => false
      )

(fact "return only items that match name"
      (items-with-name [{:name "fish"} {:name "banana"}] "fish") => [{:name "fish"}]
      )

(fact "If nothing matches, return nothing"
      (items-with-name [{:name "fish"} {:name "banana"}] "penguin") => []
      )

(fact "can't take key from fish"
      (used-from-but-not-for-room? "take key from fish") => true
      )

(fact "trying to take key from room"
      (used-from-but-not-for-room? "take key from room") => false
      )

(fact "not trying to take key from room"
      (used-from-but-not-for-room? "take key") => nil
      )

(fact "also trying to take key from room"
      (asked-from-room? "take key from room") => true
      )

(fact "not trying to take key from room"
      (asked-from-room? "take key from fish") => nil
      )

(fact "edible thing is edible"
      (edible? {:id 5 :type "edible" :name "cupcake"}) => true
      )

(fact "non edible thing is not edible"
      (edible? {:id 5 :name "cupcake"}) => false
      )

(fact "non edible thing is not edible"
      (edible? {:id 5 :name "hat" :type "wearable"}) => false
      )


(fact "drinkable thing is drinkable"
      (drinkable? {:id 5 :type "drinkable" :name "coffee"}) => true
      )


(fact "non drinkable thing is not drinkable"
      (drinkable? {:id 5 :type "wearable" :name "hat"}) => false
      )

(fact "wearable thing is wearable"
      (wearable? {:id 5 :type "wearable" :name "hat"}) => true
      )

(fact "non wearable thing is not wearable"
      (wearable? {:id 5 :type "drinkable" :name "coffee"}) => false
      )

(fact "worn treasure is worn"
      (def treasure {:id 1 :type "wearable" :name "hat"})
      (def player {:id 1 :name "bob" :treasure [treasure]})
      (treasure-worn? player treasure) => true
      (provided
            (models/worn-treasure-by-player-id 1) => [treasure]
            )
      )

(fact "all treasure mentioned"
      (def treasure1 {:id 1 :type "wearable" :name "hat"})
      (def treasure2 {:id 2 :type "wearable" :name "shoe"})
      (def treasure3 {:id 3 :type "wearable" :name "gloves"})
      (treasure-mentioned "combine hat shoe" [treasure1 treasure2 treasure3]) => [treasure1 treasure2]
      )

(fact "exits mentioned should return exit mentioned"
      (def exit1 {:id 1 :to_room 42 :from_room 1 :keywords "door" :locked 0})

      (exits-mentioned "open door" [exit1]) => [exit1])

(fact "exits mentioned should return exit mentioned if matches one of the keywords"
      (def exit1 {:id 1 :to_room 42 :from_room 1 :keywords "door exit" :locked 0})
      (def exit2 {:id 2 :to_room 43 :from_room 1 :keywords "puppy" :locked 0})

      (exits-mentioned "open door" [exit1 exit2]) => [exit1])

(fact "list hero names"
      (list-hero-names 1) => "buffy, kate"
      (provided
        (models/players-by-friend-group 1) => [{:id 1 :name "buffy"} {:id 2 :name "kate"}]))

(fact "treasure not taken by friend group returns treasure if not taken"
      (def player {:id 1 :name "bob" :treasure [] :friend_group [{:id 1}]})
      (def treasure [{:id 1 :name "fish"}])

      (treasure-not-taken-by-friend-group player treasure) => treasure
      (provided
        (models/friend-group-by-id 1) => {:id 1 :treasure []}))

(fact "treasure not taken by friend group returns nothing if treasure taken"
      (def player {:id 1 :name "bob" :treasure [] :friend_group [{:id 1}]})
      (def treasure [{:id 1 :name "fish"}])

      (treasure-not-taken-by-friend-group player treasure) => []
      (provided
        (models/friend-group-by-id 1) => {:id 1 :treasure treasure}))

(fact "treasure not taken by friend group returns treasure if no friend group"
      (def player {:id 1 :name "bob" :treasure [] :friend_group []})
      (def treasure [{:id 1 :name "fish"}])

      (treasure-not-taken-by-friend-group player treasure) => treasure)
