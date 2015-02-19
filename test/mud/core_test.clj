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