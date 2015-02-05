(ns mud.brain-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.models :as models]
            [mud.brain :refer :all]))

(fact "list one exit"
      (list-exits 1 "exits" 1) => "<p>You see 1 exits:</p> A madeup door"
      (provided
        (models/exits-by-room 1) => [{:id 1 :description "A madeup door"}]
        )
      )
