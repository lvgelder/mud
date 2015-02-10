(ns mud.combat
  (:require
    [mud.models :as models]
    [mud.core :as core]
    ))

(defn fight [player-id action room-id]
  (let [monsters (models/monster-by-room room-id)
        player (models/player-by-id player-id)
        unkilled-monsters (core/monsters-left-to-kill player monsters)
        ]
    (cond (empty? unkilled-monsters) "Nothing to fight"
          :else (
                  do
                  (models/kill-monster player-id (:id (first unkilled-monsters)))
                  (format "You killed the %s!" (:name (first unkilled-monsters)))
                  )

          )
    )
  )