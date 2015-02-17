(ns mud.treasure
  (:require
    [mud.models :as models]
    [mud.core :as core]
    [clojure.string :as str]))

(defn list-treasure-in-room [player-id action room-id]

  (defn treasure-item[name]
    (str "<li>" name "</li>")
    )

  (let [room (models/room-by-id room-id)
        treasure (:treasure room)
        player (models/player-by-id player-id)
        treasure-left-in-room (core/treasure-left player treasure)
        ]
      (format "<p>You see %s items in this room.</p> <ul>%s</ul>"
              (count treasure-left-in-room) (reduce str (map #(treasure-item (:description %)) treasure-left-in-room)))

    )
  )

(defn list-treasure-from-monster [monsters-mentioned]
  (defn treasure-item[name]
    (str "<li>" name "</li>")
    )
  (let [monster (models/monster-by-id (:id (first monsters-mentioned))) treasure (:treasure monster)]
    (format "<p>You search the %s and find %s items.</p> <ul>%s</ul>"
            (:name (first monsters-mentioned)) (count treasure) (reduce str (map #(treasure-item (:description %)) treasure)))
    )
  )

(defn search [player-id action room-id]
  (let [room (models/room-by-id room-id)
        player (models/player-by-id player-id)
        monsters (:monster room)
        monsters-left-to-kill (core/monsters-left-to-kill player monsters)
        action-list (str/split action #"\s+")
        monsters-mentioned (core/monsters-mentioned action monsters)
        ]
    (cond
      (not (empty? monsters-left-to-kill)) (format "You try to search but the %s tries to eat you..." (:name (first monsters-left-to-kill)))
      (= action "search") (list-treasure-in-room player-id action room-id)
      (= action "search room") (list-treasure-in-room player-id action room-id)
      (empty? monsters-mentioned) (format "You can't search the %s because there is no %s." (second action-list) (second action-list))
      (not (empty? monsters-mentioned)) (list-treasure-from-monster monsters-mentioned)
      :else "I don't know what you are searching for!"
      )
    )
  )

(defn has-five-items-or-more[player]
  (>= (count (:treasure player)) 5)
  )

(defn take-item-from-room [player-id action room-id]
  (let [room (models/room-by-id room-id)
        treasure (:treasure room)
        treasure-to-take (first (core/treasure-mentioned action treasure))
        monsters (:monster room)
        player (models/player-by-id player-id)
        ]
    (cond
      (empty? treasure-to-take) "You can't take that."
      (core/monsters-left-to-kill? player monsters) (str (format "You can't take it because the %s tries to eat you." (:name (first monsters))))
      (core/already-taken-treasure? player treasure-to-take) "You already have that."
      (has-five-items-or-more player) "You already have 5 items. You need to drop something."
      :else (
              do
              (models/collect-treasure player-id (:id treasure-to-take))
              (str (format "You have the %s." (:name treasure-to-take)))
              )
      )
    )
  )

(defn drop-item [player-id action room-id]
  (let [player (models/player-by-id player-id)
        treasure (:treasure player)
        droppable-treasure (core/treasure-mentioned action treasure)]
    (if (empty? droppable-treasure)
      "You don't have that."
      (do
        (models/remove-treasure-from-player (:id player) (:id (first droppable-treasure)))
        (str (format "You put the %s down." (:name (first droppable-treasure))))
        )

      )

    )
  )

(defn take-what [player-id action room-id]
  (let [player (models/player-by-id player-id)
        room (models/room-by-id room-id)
        unkilled-monsters (core/monsters-left-to-kill player (:monster room))]
    (cond
      (not (empty? unkilled-monsters)) (format "You can't take that because the %s tries to eat you." (:name (first unkilled-monsters)))
      )
    )
  )




