(ns mud.chat-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [mud.chat :refer :all]
            [mud.models :as models]
            [mud.messages :as messages]))

(fact "list nobody if player is not in a friend group"
      (def player {:id 1})

      (list-players player 1) => ""
      (provided
        (present-players-in-friend-group player 1) => [] ))

(fact "list bob if he is currently playing and in your friend group"
      (def pl1 {:name "bob" :id 5})
      (def current-player {:id 1 :name "sue" :friend_group [{:id 1}]})

      (present-players-in-friend-group current-player 1) => [pl1]
      (provided
        (models/find-players-in-room 1) => [pl1]
        (messages/currently-playing 5) => true
      (models/players-by-friend-group 1) => [pl1]))

(fact "dont list bob if he is not currently playing"
      (def pl1 {:name "bob" :id 5})
      (def current-player {:id 1 :name "sue" :friend_group [{:id 1}]})

      (present-players-in-friend-group current-player 1) => []
      (provided
        (models/find-players-in-room 1) => [pl1]
        (messages/currently-playing 5) => false
        (models/players-by-friend-group 1) => [pl1]))

(fact "dont list current player as being in the room"
      (def currPlayer {:name "me" :id 1 :friend_group [{:id 1}]})
      (def pl1 {:name "bob" :id 5})
      (list-players currPlayer 1) => "bob is in the room with you."
      (provided
        (present-players-in-friend-group currPlayer 1) => [pl1]))

(fact "say doesnt work if player name unknown"
      (say 1 "say hello to bob" 1) => "I don't know who bob is."
      (provided
        (models/player-by-id 1) => {:id 1 :name "kate"}
        (models/player-by-name "bob") => nil))

(fact "say doesnt work if you don't say who to say to"
      (say 1 "say hello" 1) => "I don't know how to do that.")

(fact "say works if other user exists, is playing and is in the same room."
      (say 1 "say hello to bob" 1) => "<p>You said hello to bob.</p>"
      (provided
        (models/player-by-id 1) => {:id 1 :name "kate"}
        (models/player-by-name "bob") => {:id 5 :username "bob" :room [{:id 1 :description "A room"}]}
        (messages/currently-playing 5) => true
        (messages/messsage "<p>kate says hello</p>" 5) => irrelevant :times 1 ))

(fact "say works for player name with odd spacing"
      (say 1 "say hello to   bob the builder " 1) => "<p>You said hello to bob the builder.</p>"
      (provided
        (models/player-by-id 1) => {:id 1 :name "kate"}
        (models/player-by-name "bob the builder") => {:id 5 :username "bob the builder" :room [{:id 1 :description "A room"}]}
        (messages/currently-playing 5) => true
        (messages/messsage "<p>kate says hello</p>" 5) => irrelevant :times 1 ))

(fact "you can't say if player not currently playing."
      (say 1 "say hello to bob" 1) => "<p>bob is not there to talk to.</p>"
      (provided
        (models/player-by-id 1) => {:id 1 :name "kate"}
        (models/player-by-name "bob") => {:id 5 :username "bob" :room [{:id 1 :description "A room"}]}
        (messages/currently-playing 5) => false
        (messages/messsage "<p>kate says hello</p>" 5) => irrelevant :times 0 ))

(fact "you can't say if player not in same room as you."
      (say 1 "say hello to bob" 1) => "<p>bob is not there to talk to.</p>"
      (provided
        (models/player-by-id 1) => {:id 1 :name "kate"}
        (models/player-by-name "bob") => {:id 5 :username "bob" :room [{:id 8 :description "A room"}]}
        (messages/currently-playing 5) => true
        (messages/messsage "<p>kate says hello</p>" 5) => irrelevant :times 0 ))