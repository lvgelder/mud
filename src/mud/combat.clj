(ns mud.combat
  (:require
    [mud.models :as models]
    ))

(defn fight [player_id action room_id]
  (let [monster (models/monster-by-room room_id) killed (models/monsters_killed player_id (:id monster))]
    (if (or (empty? monster) (not (empty? killed)))
      "Nothing to fight"
      (
        do
        (models/kill-monster player_id (:id monster))
        (format "You killed the %s!" (:name monster))
        )
      )
    )
  )
