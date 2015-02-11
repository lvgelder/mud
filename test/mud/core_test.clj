(ns mud.core-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.core :refer :all]
            ))

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


