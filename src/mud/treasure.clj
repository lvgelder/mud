(ns mud.treasure
  (:require
    [mud.models :as models]
    [mud.core :as core]
    [clojure.string :as str]
    [mud.chat :as chat]))

(defn list-treasure-in-room [player-id action room-id]

  (let [room (models/room-by-id room-id)
        treasure (:treasure room)
        player (models/player-by-id player-id)
        treasure-not-eaten (core/treasure-not-eaten player treasure)
        treasure-not-taken-by-player (core/treasure-not-taken-by-player player treasure-not-eaten)
        treasure-left-in-room (core/treasure-not-taken-by-friend-group player treasure-not-taken-by-player)
        ]
      (format "<p>You see %s items in this room.</p> <ul>%s</ul>"
              (count treasure-left-in-room) (core/list-items treasure-left-in-room))))

(defn list-treasure-from-monster [player monsters-mentioned]

  (let [monster (models/monster-by-id (:id (first monsters-mentioned)))
        treasure (:treasure monster)
        treasure-not-eaten (core/treasure-not-eaten player treasure)
        treasure-not-taken-by-player (core/treasure-not-taken-by-player player treasure-not-eaten)
        treasure-left (core/treasure-not-taken-by-friend-group player treasure-not-taken-by-player)
        ]
    (format "<p>You search the %s and find %s items.</p> <ul>%s</ul>"
            (:name (first monsters-mentioned)) (count treasure-left) (core/list-items treasure-left))))

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
      (not (empty? monsters-mentioned)) (list-treasure-from-monster player monsters-mentioned)
      :else "I don't know what you are searching for!")))

(defn has-five-items-or-more[player]
  (>= (count (:treasure player)) 5))

(defn take-item [player room-id treasure-to-take]
  (cond
    (empty? treasure-to-take) "You can't take that."
    (core/already-taken-treasure? player treasure-to-take) "You already have that."
    (core/treasure-taken-by-friend-group? player treasure-to-take) "Someone in your group already has that."
    (has-five-items-or-more player) "You already have 5 items. You need to drop something."
    :else (
            do
            (models/collect-treasure (:id player) (:id treasure-to-take))
            (if (:friend_group player)
              (do
              (models/collect-treasure-for-friend-group (:id (first (:friend_group player))) (:id treasure-to-take))
              (chat/notify-taken-treasure player room-id treasure-to-take)))
            (str (format "You have the %s." (:name treasure-to-take))))))

(defn take-item-from-room [player-id action room-id]
  (let [room (models/room-by-id room-id)
        treasure (:treasure room)
        treasure-to-take (first (core/treasure-mentioned action treasure))
        player (models/player-by-id player-id)]
    (take-item player room-id treasure-to-take)))

(defn take-item-from-monster [player action room-id monsters-mentioned]
  (let [monster (models/monster-by-id (:id (first monsters-mentioned)))
        treasure-to-take (first (core/treasure-mentioned action (:treasure monster)))]
    (take-item player room-id treasure-to-take)))

(defn drop-item [player-id action room-id]
  (let [player (models/player-by-id player-id)
        treasure (:treasure player)
        droppable-treasure (core/treasure-mentioned action treasure)]
    (if (empty? droppable-treasure)
      "You don't have that."
      (do
        (models/remove-treasure-from-player (:id player) (:id (first droppable-treasure)))
        (str (format "You put the %s down." (:name (first droppable-treasure))))))))

(defn take-what [player-id action room-id]
  (let [player (models/player-by-id player-id)
        room (models/room-by-id room-id)
        unkilled-monsters (core/monsters-left-to-kill player (:monster room))
        monsters-mentioned (core/monsters-mentioned action (:monster room))]
    (cond
      (not (empty? unkilled-monsters)) (format "You can't take that because the %s tries to eat you." (:name (first unkilled-monsters)))
      (and (empty? monsters-mentioned) (core/used-from-but-not-for-room? action)) "You can't take that."
      (or (empty? monsters-mentioned) (core/asked-from-room? action)) (take-item-from-room player-id action room-id)
      (not (empty? monsters-mentioned)) (take-item-from-monster player action room-id monsters-mentioned))))

(defn restore-hit-points [player treasure]
  (if (< (:hit_points player) (:max_hit_points player))
    (let [new-hit-points (+ (:hit_points player) (:hit_points treasure))]
      (if (> new-hit-points (:max_hit_points player))
        (models/set-hit-points (:id player) (:max_hit_points player))
        (models/set-hit-points (:id player) new-hit-points)))))

(defn eat [player-id action room-id]
  (let [player (models/player-by-id player-id)
        treasure-mentioned (first (core/treasure-mentioned action (:treasure player)))]
    (cond
      (empty? treasure-mentioned) "I don't know what that is."
      (not (core/edible? treasure-mentioned )) "You can't eat that."
      :else
      ( do
        (models/remove-treasure-from-player player-id (:id treasure-mentioned))
        (models/eat-treasure player-id (:id treasure-mentioned))
        (restore-hit-points player treasure-mentioned)
        (format "You eat the %s. %s" (:name treasure-mentioned) (:action_description treasure-mentioned ))))))

(defn drink [player-id action room-id]
  (let [player (models/player-by-id player-id)
        treasure-mentioned (first (core/treasure-mentioned action (:treasure player)))]
    (cond
      (empty? treasure-mentioned) "I don't know what that is."
      (not (core/drinkable? treasure-mentioned)) "You can't drink that."
      :else
      ( do
        (models/remove-treasure-from-player player-id (:id treasure-mentioned))
        (models/eat-treasure player-id (:id treasure-mentioned))
        (restore-hit-points player treasure-mentioned)
        (format "You drink the %s. %s" (:name treasure-mentioned) (:action_description treasure-mentioned ))))))

(defn wear [player-id action room-id]
  (let [player (models/player-by-id player-id)
        treasure-mentioned (first (core/treasure-mentioned action (:treasure player)))]
    (cond
      (empty? treasure-mentioned) "I don't know what that is."
      (not (core/wearable? treasure-mentioned)) "You can't wear that."
      (core/treasure-worn? player treasure-mentioned) "You are already wearing that."
      :else
      ( do
        (models/wear_treasure player-id (:id treasure-mentioned))
        (format "You put on the %s. %s" (:name treasure-mentioned) (:action_description treasure-mentioned ))))))



(defn combine-items [player-id treasure]
  (let [combined-treasure (first (models/find-combined-treasure (:id (first treasure))))]
    do
    (doall (for [t treasure] (models/remove-treasure-from-player player-id (:id t))))
    (doall (for [t treasure] (models/eat-treasure player-id (:id t))))
    (models/collect-treasure player-id (:id combined-treasure))
    (:action_description combined-treasure)))

(defn combine-treasure [player-id action room-id]
  (let [player (models/player-by-id player-id)
        treasure-mentioned (core/treasure-mentioned action (:treasure player))
        combinable-items (models/find-combinable-items (:id (first treasure-mentioned)))]
    (cond
      (empty? treasure-mentioned) "You don't have that."
      (not (every? core/combinable? treasure-mentioned )) "You can't combine that."
      (not (= (set treasure-mentioned) (set combinable-items))) "You can't combine that."
      (= (set treasure-mentioned) (set combinable-items)) (combine-items player-id treasure-mentioned))))




