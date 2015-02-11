(ns mud.brain-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.brain :refer :all]
            [mud.exits :as exits]
            [mud.treasure :as treasure]))

(fact "Asking to do something it doesnt understand should return that"
      (action 1 "madeup string of foo" 1) => "I don't know how to do that"
      )

(fact "Asking for exits should call list-exits"
      (action 1 "exits" 1 ) => irrelevant
      (provided
        (exits/list-exits 1 "exits" 1) => irrelevant :times 1
        )
      )

(fact "Asking for open door should call take exit"
      (action 1 "open door" 1) => irrelevant
      (provided
        (exits/take-exit 1 "open door" 1) => irrelevant :times 1
        )
      )

(fact "Asking to use something should ask if we are using a key"
      (use-what 1 "use key" 1) => irrelevant
      (provided
        (exits/using-key? "use key") => true
        (exits/take-exit 1 "use key" 1) => irrelevant :times 1
        )
      )

(fact "Asking to use something should ask if we are using a key"
      (use-what 1 "use key" 1) => "I don't know how to use that."
      (provided
        (exits/using-key? "use key") => false
        (exits/take-exit 1 "use key" 1) => irrelevant :times 0
        )
      )

(fact "Asking to use something should ask if we are using a key"
      (try-what 1 "try key" 1) => irrelevant
      (provided
        (exits/using-key? "try key") => true
        (exits/take-exit 1 "try key" 1) => irrelevant :times 1
        )
      )

(fact "Asking to use something should ask if we are using a key"
      (try-what 1 "try key" 1) => "I don't know how to try that."
      (provided
        (exits/using-key? "try key") => false
        (exits/take-exit 1 "try key" 1) => irrelevant :times 0
        )
      )
