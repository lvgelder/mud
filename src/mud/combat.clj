(ns mud.combat
  (:require
    [mud.models :as models]
    [mud.core :as core]
    ))


(defn win [player monster]
  do
  (models/set-hit-points (:id player) (:hit_points player))
  (models/kill-monster (:id player) (:id monster))
  (format "You killed the %s!" (:name monster)))

(defn lose [player monster]
  do
  (models/remove-all-treasure-from-player (:id player))
  (models/remove-all-monsters-from-player (:id player))
  (models/set-hit-points (:id player) 5)
  (models/set-max-hit-points (:id player) 5)
  (models/set-player-room (:id player) 1)
  (format "The %s killed you! What a shame. It was a brave fight." (:name monster)))


(defn hit [creature, weapon]
  (- (:hit_points creature) (rand-int (:damage weapon))))

(defn hit-creature [attacker, attackee]
  (assoc attackee :hit_points (hit attackee (:weapon attacker))))

(defn fight [player action monster]
  (let [player-with-weapon (models/player-with-weapon (:id player))
        monster-with-weapon (models/monster-with-weapon (:id monster))
        player-hit (hit-creature monster-with-weapon player-with-weapon)
        monster-hit (hit-creature player-with-weapon monster-with-weapon)
        ]
    (cond
      (<= (:hit_points player-hit) 0) (lose player-hit monster-hit)
      (<= (:hit_points monster-hit) 0) (win player-hit monster-hit)
      :else (format "You swing at the %s with your %s and hit, but it is still standing. And looking angry now." (:name monster) (:name (:weapon player-with-weapon))))))

(defn fight-what [player-id action room-id]
  (let [monsters (models/monster-by-room room-id)
        player (models/player-by-id player-id)
        unkilled-monsters (core/monsters-left-to-kill player monsters)
        monsters-mentioned (core/monsters-mentioned action unkilled-monsters)]
    (cond
      (empty? unkilled-monsters) "Nothing to fight"
      (empty? monsters-mentioned) "You can't fight that."
      (not (empty? monsters-mentioned)) (fight player action (first monsters-mentioned))
      :else "I don't know how to fight that." )))
