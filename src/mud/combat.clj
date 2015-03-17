(ns mud.combat
  (:require
    [mud.models :as models]
    [mud.core :as core]
    ))


(defn win [player monster]
  do
  (models/reset-fight-in-progress (:id player))
  (models/set-hit-points (:id player) (:hit_points player))
  (models/kill-monster (:id player) (:id monster))
  (format "You killed the %s!" (:name monster)))

(defn lose [player monster]
  do
  (models/reset-fight-in-progress (:id player))
  (models/remove-all-treasure-from-player (:id player))
  (models/remove-all-monsters-from-player (:id player))
  (models/set-hit-points (:id player) 5)
  (models/set-max-hit-points (:id player) 5)
  (models/set-player-room (:id player) 1)
  (format "The %s killed you! What a shame. It was a brave fight." (:name monster)))


(defn hit [creature, weapon]
  (- (:hit_points creature) (rand-int (:damage weapon))))

(defn hit-creature [attacker, attackee]
  (assoc attackee :hit_points (hit attackee (first (:weapon attacker)))))

(defn current-monster-hitpoints [player-id monster]
  (let [cur-fight-in-progress (models/select_fight_in_progress player-id (:id monster))]
    (if (not-empty cur-fight-in-progress)
      (assoc monster :hit_points (:monster_hit_points (first cur-fight-in-progress)))
      monster)))

(defn update-hit-points [player monster]
  (let [fight-in-progress (models/select_fight_in_progress (:id player) (:id monster))]
    (if (empty? fight-in-progress)
      (models/insert_fight_in_progress (:id player) (:id monster) (:hit_points monster))
      (models/update-fight-in-progress (:id player) (:id monster) (:hit_points monster))
      )))


(defn fight [player action monster]
  (let [player-with-weapon (models/player-with-weapon (:id player))
        monster-with-weapon (models/monster-with-weapon (:id monster))
        current-monster-hitpoints (current-monster-hitpoints (:id player) monster-with-weapon)
        player-hit (hit-creature monster-with-weapon player-with-weapon)
        monster-hit (hit-creature player-with-weapon current-monster-hitpoints)
        ]
    (cond
      (<= (:hit_points player-hit) 0) (lose player-hit monster-hit)
      (<= (:hit_points monster-hit) 0) (win player-hit monster-hit)
      :else
      ( do
        (update-hit-points player-hit monster-hit)
        (models/set-hit-points (:id player) (:hit_points player-hit))
        (format "You swing at the %s with your %s and hit, but it is still standing. And looking angry now." (:name monster) (:name (first (:weapon player-with-weapon))))))))

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
