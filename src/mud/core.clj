(ns mud.core)

(defn seq-contains? [coll target] (some #(= target %) coll))

(defn seq-contains-id? [coll target] (some #(= (:id target) (:id %)) coll))

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