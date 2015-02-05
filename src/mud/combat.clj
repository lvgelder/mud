(ns mud.combat
  (:require
    [mud.models :as models]
    ))

(defn fight [player_id action room_id]
  (let [monster (models/monster-by-room room_id) killed (models/monsters_killed player_id (:id monster))]
    (cond (empty? monster) "Nothing to fight"
          (not (empty? killed)) (format "You already fought the %s and won. It is lying dead before you..." (:name monster))
          :else (
                  do
                  (models/kill-monster player_id (:id monster))
                  (format "You killed the %s!" (:name monster))
                  )

          )
    )
  )