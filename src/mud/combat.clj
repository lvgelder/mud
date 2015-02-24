(ns mud.combat
  (:require
    [mud.models :as models]
    [mud.core :as core]
    ))


(defn win [player-id monster]
  do
  (models/kill-monster player-id (:id monster))
  (format "You killed the %s!" (:name monster)))

(defn lose [player-id monster]
  do
  (models/remove-all-treasure-from-player player-id)
  (models/remove-all-monsters-from-player player-id)
  (models/set-hit-points player-id 5)
  (models/set-max-hit-points player-id 5)
  (models/set-player-room player-id 1)
  (format "The %s killed you! What a shame. It was a brave fight." (:name monster)))

(defn fight [player action monster]
  (win (:id player) (:id monster)))

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
