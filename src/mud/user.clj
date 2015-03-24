(ns mud.user
  (:require
    [ring.util.response :as response]
    [mud.models :as models]
    [mud.validations :as valid]))


(defn new-user [params]
  (models/create-player params)
  (models/create-user params)
  (let [pl (models/player-by-name (:name params))
        usr (models/find-by-username (:username params))]
    (models/initialize-player-room (:id pl) 1)
    (models/initialize-player-weapon (:id pl))
    (models/add-user-role (:id usr) 1)
    (models/add-user-player (:id usr) (:id pl))
    )
  (assoc (response/redirect "/login") :flash "New player created successfully! Please login with your new credentials."))

(defn make-player [params]
  (let [err (valid/valid-user? params)]
    (if (not(empty? err))
      (assoc (response/redirect "/player/new") :flash (assoc err :form-vals {:username (:username params)
                                                                             :password (:password params) :name (:name params)}))
      (new-user params))))