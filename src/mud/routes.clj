(ns mud.routes
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as response]
            [mud.views :as views]
            [mud.models :as models]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [clojure.string :as str]))


(def users {"admin" {:username "admin"
                     :password (creds/hash-bcrypt "password")
                     :roles #{::admin}}
            "dave" {:username "dave"
                    :password (creds/hash-bcrypt "password")
                    :roles #{::user}}})
;
(defroutes app-routes
           (GET "/" [] ;;(1)
                (views/index))
           (GET "/entrance" req
                (views/entrance req))
           (GET "/player/new" []
                (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                  (GET "/login" [] "Here is our login page.")
                                  (views/new-player)))
           (GET "/login" [] (views/login))
           (GET "/signup"[] (views/sign-up))
           (GET "/logout" []
                (friend/logout* (response/redirect "/entrance") ))
           (GET "/player/:id" [id] ;;(2)
                (friend/authorize #{::user} "This page can only be seen by authenticated users."
                (GET "/login" [] "Here is our login page.")
                (views/player id)))
           (POST "/signup" [& params]
                 (views/create-user params))
           (POST "/players" [& params]
                 (views/make-player params))
           (POST "/actions" [& params]
                 (views/action params))
           )

(defn find-user-and-role [usr]
  (let [usr-from-db (models/find-by-username (:username usr))]
    (if usr-from-db
      {(:username usr) (assoc usr-from-db :roles #{::user})}
      {}
      )
    )
  )

(def app
  (handler/site
  (friend/authenticate app-routes {
                                   :login-uri "/login"
                                   :credential-fn #(creds/bcrypt-credential-fn (find-user-and-role %) %)
                                   :workflows [(workflows/interactive-form)]})
                       (wrap-keyword-params app-routes)
                       (wrap-params app-routes)))

;
