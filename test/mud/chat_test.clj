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

(fact "say doesnt work if username unknown"
      (say 1 "say hello to bob" 1) => "I don't know who bob is."
      (provided
        (models/player-by-id 1) => {:id 1 :name "kate"}
        (models/player-by-name "bob") => nil))

(fact "say doesnt work if you don't say who to say to"
      (say 1 "say hello" 1) => "I don't know how to do that.")

(fact "say works if user"
      (say 1 "say hello to bob" 1) => "You said hello to bob."
      (provided
        (models/player-by-id 1) => {:id 1 :name "kate"}
        (models/player-by-name "bob") => {:id 5 :username "bob"}
        (messages/currently-playing 5) => true
        (messages/messsage "kate says hello" 5) => irrelevant :times 1 ))

(fact "say works for player name with odd spacing"
      (say 1 "say hello to   bob the builder " 1) => "You said hello to bob the builder."
      (provided
        (models/player-by-id 1) => {:id 1 :name "kate"}
        (models/player-by-name "bob the builder") => {:id 5 :username "bob the builder"}
        (messages/currently-playing 5) => true
        (messages/messsage "kate says hello" 5) => irrelevant :times 1 ))

(fact "you can't say if player not currently playing."
      (say 1 "say hello to bob" 1) => "bob is not there to talk to."
      (provided
        (models/player-by-id 1) => {:id 1 :name "kate"}
        (models/player-by-name "bob") => {:id 5 :username "bob"}
        (messages/currently-playing 5) => false
        (messages/messsage "kate says hello" 5) => irrelevant :times 0 ))