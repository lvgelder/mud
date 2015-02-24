(ns mud.combat-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.combat :refer :all]
            [mud.models :as models]
            [mud.brain :as brain]))


(fact "Cant kill the same monster twice"
      (fight-what 1 "fight vampire" 1) => "Nothing to fight"
      (provided
        (models/monster-by-room 1) => [{:id 1 :name "vampire"}]
        (models/player-by-id 1) => {:id 1 :monster [{:id 1 :name "vampire"}]}
        )
      )

(fact "Cant kill a monster if there isn't one"
      (fight-what 1 "fight vampire" 1) => "Nothing to fight"
      (provided
        (models/monster-by-room 1) => []
        (models/player-by-id 1) => {:id 1 :monster []}
        )
      )

(fact "Cant fight a monster that isn't there"
      (fight-what 1 "fight squid" 1) => "You can't fight that."
      (provided
        (models/monster-by-room 1) => [{:id 1 :name "vampire"}]
        (models/player-by-id 1) => {:id 1 :monster []}
        )
      )

(fact "If there is a monster and you ask to fight it pass on to fight function."
      (def player {:id 1 :monster []})
      (def monster {:id 1 :name "vampire"})

      (fight-what 1 "fight vampire" 1) => irrelevant
      (provided
        (models/monster-by-room 1) => [monster]
        (models/player-by-id 1) => player
        (fight player "fight vampire" monster) => irrelevant :times 1
        )
      )

(fact "Combat kills a monster"
      (def player {:id 1 :monster []})
      (def monster {:id 1 :name "vampire"})

      (fight player "fight vampire" monster) => irrelevant
      (provided
        (win 1 1) => irrelevant :times 1
        )
      )