(ns mud.validations
  (require [valip.core :refer [validate]]
           [valip.predicates :refer [present? min-length]]))


(defn valid-user? [usr]
  (validate usr
            [:username present? "Username cannot be blank"]
            [:username (min-length 3) "Username must be at least three characters long"]
            [:password present? "Password cannot be blank"]
            [:password (min-length 4) "Password must be at least four characters long"]
            [:name present? "Hero name cannot be blank"]
            [:name (min-length 3) "Hero name must be at least three characters long"]))
