(ns mud.core
  (:require [clojure.string :as str]
            [mud.models :as models]))

(defn seq-contains? [coll target] (some #(= target %) coll))

(defn seq-contains-id? [coll target] (some #(= (:id target) (:id %)) coll))

(defn items-with-name [coll name]
  (filter #(= (:name %) name) coll)
  )

(defn contains-item-with-id [items item]
  (seq-contains-id? items item)
  )

(defn not-contains-item-with-id [items item]
  (not (seq-contains-id? items item))
  )

(defn monsters-left-to-kill? [player monsters]
  (let [killed-monsters (filter #(contains-item-with-id (:monster player) %) monsters)]
    (not (= (count killed-monsters) (count monsters)))
    )
  )

(defn monsters-left-to-kill [player monsters]
  (filter #(not-contains-item-with-id (:monster player) %) monsters)
  )

(defn already-taken-treasure? [player treasure]
    (contains-item-with-id (:treasure player) treasure)
  )

(defn not-already-taken-treasure [player treasure]
  (not (already-taken-treasure? player treasure) )
  )

(defn treasure-left [player treasure]
  (filter #(not-already-taken-treasure player %) treasure)
  )

(defn treasure-not-eaten [player treasure]
  (let [treasure-eaten (models/eaten-treasure-by-player-id (:id player))]
    (filter #(not-contains-item-with-id treasure-eaten %) treasure)
    )
  )

(defn help [player-id action room-id]
  "Try looking around. Try searching. If there is a monster, try fighting it. If there is a door, try opening it."
  )

(defn treasure-mentioned [action treasure]
  (let [action-list (str/split action #"\s+")]
    (filter #(seq-contains? action-list (:name %)) treasure)
    )
  )

(defn monsters-mentioned [action monsters]
  (let [action-list (str/split action #"\s+")]
    (filter #(seq-contains? action-list (:name %)) monsters)
    )
  )

(defn exits-mentioned [action exits]
  (let [action-list (str/split action #"\s+")]
    (filter #(seq-contains? action-list (:keywords %)) exits)
    )
  )
(defn asked-from-room?[action]
  (let [action-list (str/split action #"\s+")]
    (and (seq-contains? action-list "from") (seq-contains? action-list "room"))
    )
  )

(defn used-from-but-not-for-room? [action]
  (let [action-list (str/split action #"\s+")]
    (and (seq-contains? action-list "from") (not (asked-from-room? action)))
    )
  )

(defn edible? [treasure]
  (= (:type treasure) "edible")
  )

(defn drinkable? [treasure]
  (= (:type treasure) "drinkable")
  )

(defn wearable? [treasure]
  (= (:type treasure) "wearable")
  )

(defn combinable? [treasure]
  (= (:type treasure) "combinable")
  )

(defn treasure-worn? [player treasure]
  (let [treasure-worn (models/worn-treasure-by-player-id (:id player))]
    (contains-item-with-id treasure-worn treasure)
    )
  )

(defn get-player-from-identity [identity]
  (let [username (:current identity)
        auths ((:authentications identity) username)]
    (first (:player auths))))