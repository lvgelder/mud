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