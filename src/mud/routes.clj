(ns mud.routes
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.util.response :as response]
            [mud.views :as views]
            [mud.models :as models]
            [compojure.route :as route]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(defroutes app-routes
           (GET "/" []
                (views/index))
           (GET "/player/new" req
                (views/new-player req))
           (GET "/friend-group/new" req
                (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                  (GET "/login" [] "Here is our login page.")
                                  (views/new-friend-group req)))
           (GET "/login" req (views/login req))
           (GET "/logout" []
                (friend/logout* (response/redirect "/player") ))
           (GET "/actions" []
                (response/redirect "/player"))
           (GET "/player" req
                (friend/authorize #{::user} "This page can only be seen by authenticated users."
                (GET "/login" [] "Here is our login page.")
                (views/player req)))
           (POST "/players" [& params]
                 (views/make-player params))
           (POST "/friend-group" req
                 (views/make-friend-group req))
           (POST "/actions" req
                 (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                   (GET "/login" [] "Here is our login page.")
                                   (views/action req)))
           (route/resources "/")
           (route/not-found "<h1>Page not found</h1>"))

(defn find-user-and-role [usr]
  (let [usr-from-db (models/find-by-username (:username usr))]
    (if usr-from-db
      {(:username usr) (assoc usr-from-db :roles #{::user})}
      {})))

(def app
  (handler/site
    (friend/authenticate app-routes {
                                     :login-uri     "/login"
                                     :credential-fn #(creds/bcrypt-credential-fn (find-user-and-role %) %)
                                     :workflows     [(workflows/interactive-form)]})
    (wrap-flash app-routes)
    (wrap-keyword-params app-routes)
    (wrap-params app-routes)))


