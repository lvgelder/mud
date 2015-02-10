(ns mud.combat-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.combat :refer :all]
            [mud.models :as models]
            [mud.brain :as brain]))

(fact "Combat kills a monster"
      (fight 1 "" 1) => "You killed the vampire!"
      (provided
        (models/player-by-id 1) => {:id 1 :monster []}
        (models/monster-by-room 1) => [{:id 1 :name "vampire"}]
        )
      )

(fact "Cant kill the same monster twice"
      (fight 1 "" 1) => "Nothing to fight"
      (provided
        (models/monster-by-room 1) => [{:id 1 :name "vampire"}]
        (models/player-by-id 1) => {:id 1 :monster [{:id 1 :name "vampire"}]}
        )
      )

(fact "Cant kill a monster if there isn't one"
      (fight 1 "" 1) => "Nothing to fight"
      (provided
        (models/monster-by-room 1) => []
        (models/player-by-id 1) => {:id 1 :monster []}
        )
      )