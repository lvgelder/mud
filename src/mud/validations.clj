(ns mud.validations
  (require
    [mud.models :as models]
    [valip.core :refer [validate]]
    [valip.predicates :refer [present? min-length]]))

(defn username-taken?
  []
  (fn [s]
    (empty? (models/find-by-username s))))


(defn hero-name-taken?
  []
  (fn [s]
    (empty? (models/player-by-name s))))


(defn valid-user? [usr]
  (validate usr
            [:username present? "Username cannot be blank"]
            [:username (min-length 3) "Username must be at least three characters long"]
            [:username (username-taken?) "Username has already been taken"]
            [:password present? "Password cannot be blank"]
            [:password (min-length 4) "Password must be at least four characters long"]
            [:name present? "Hero name cannot be blank"]
            [:name (min-length 3) "Hero name must be at least three characters long"]
            [:name (hero-name-taken?) "Hero name has alrady been taken"]
            ))

