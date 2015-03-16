(ns mud.chat-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.chat :refer :all]
            [mud.models :as models]
            [mud.messages :as messages]))

(fact "list nobody if no players"
      (list-players [] 1) => "")

(fact "list bob if he is currently playing"
      (def pl1 {:name "bob" :id 5})
      (list-players [pl1] 1) => "bob is in the room with you."
      (provided
        (messages/currently-playing 5) => true))

(fact "dont list bob if he is not currently playing"
      (def pl1 {:name "bob" :id 5})
      (list-players [pl1] 1) => ""
      (provided
        (messages/currently-playing 5) => false))

(fact "dont list current player as being in the room"
      (def currPlayer {:name "me" :id 1})
      (def pl1 {:name "bob" :id 5})
      (list-players [pl1 currPlayer] 1) => "bob is in the room with you."
      (provided
        (messages/currently-playing 5) => true))