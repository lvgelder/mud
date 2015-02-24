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


(fact "hitting a vampire with a weapon does damage based on the weapon"
      (def player {:id 1 :weapon {:id 1 :damage 2}})
      (def monster {:id 2 :hit_points 5})
      (hit-creature player monster) => {:id 2 :hit_points 3}
      (provided
        (rand-int 2) => 2
        )
      )

(fact "if you are reduced to less than 0 hit points you lose"
      (def player {:hit_points 1 :id 1 :weapon {:id 1 :damage 2}})
      (def monster {:id 2 :hit_points 5 :weapon {:id 2 :damage 5}})

      (fight player "fight vampire" monster) => irrelevant
      (provided
        (models/player-with-weapon 1) => player
        (models/monster-with-weapon 2) => monster
        (current-monster-hitpoints 1 monster) => monster
        (rand-int 2) => 2
        (rand-int 5) => 5
        (lose {:hit_points -4 :id 1 :weapon {:id 1 :damage 2}} {:id 2 :hit_points 3 :weapon {:id 2 :damage 5}}) => irrelevant :times 1
        )
      )


(fact "if monster is reduced to less than 0 hit points you win"
      (def player {:hit_points 5 :id 1 :weapon {:id 1 :damage 5}})
      (def monster {:id 2 :hit_points 5 :weapon {:id 2 :damage 2}})

      (fight player "fight vampire" monster) => irrelevant
      (provided
        (models/player-with-weapon 1) => player
        (models/monster-with-weapon 2) => monster
        (current-monster-hitpoints 1 monster) => monster
        (rand-int 2) => 2
        (rand-int 5) => 5
        (win {:hit_points 3 :id 1 :weapon {:id 1 :damage 5}} {:id 2 :hit_points 0 :weapon {:id 2 :damage 2}}) => irrelevant :times 1
        )
      )

(fact "set current monster hitpoints to whatever it was in fight in progress"
      (def monster {:id 2 :hit_points 5 :weapon {:id 2 :damage 2}})
      (def monster_with_less_hitpoints {:id 2 :hit_points 2 :weapon {:id 2 :damage 2}})
      (current-monster-hitpoints 1 monster) => monster_with_less_hitpoints
      (provided
        (models/fight_in_progress 1 2) => {:player_id 1 :monster_id 2 :monster_hit_points 2}
        )
      )

(fact "return original monster if no fight in progress"
      (def monster {:id 2 :hit_points 5 :weapon {:id 2 :damage 2}})
      (current-monster-hitpoints 1 monster) => monster
      (provided
        (models/fight_in_progress 1 2) => []
        )
      )

(fact "if neither you nor monster has 0 hit points set hit points and continue"
      (def player {:hit_points 5 :id 1 :weapon {:id 1 :damage 5 :name "fists"}})
      (def monster {:id 2 :name "vampire" :hit_points 5 :weapon {:id 2 :damage 2}})

      (fight player "fight vampire" monster) => "You swing at the vampire with your fists and hit, but it is still standing. And looking angry now."
      (provided
        (models/player-with-weapon 1) => player
        (models/monster-with-weapon 2) => monster
        (current-monster-hitpoints 1 monster) => monster
        (hit-creature monster player) => player
        (hit-creature player monster) => monster
        (update-hit-points player monster) => irrelevant :times 1
        )
      )