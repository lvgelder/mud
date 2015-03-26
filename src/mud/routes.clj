(ns mud.routes
  (:use compojure.core
        ring.util.response
        ring.middleware.cors
        org.httpkit.server)
  (:require [compojure.handler :as handler]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.flash :refer [wrap-flash]]
            [ring.middleware.reload :as reload]
            [ring.util.response :as response]
            [mud.views :as views]
            [mud.friend_group :as friend_group]
            [mud.game :as game]
            [mud.user :as mud_user]
            [mud.models :as models]
            [compojure.route :as route]
            [cemerick.friend :as friend]
            [cheshire.core :refer :all]
            [environ.core :refer [env]]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [mud.messages :as messages]))

(defroutes app-routes
           (GET "/" []
                (views/index))
           (GET "/messages" req
                (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                  (GET "/login" [] "Here is our login page.")
                                  messages/ws))
           (GET "/player/new" req
                (views/new-player req))
           (GET "/friend-group" req
                (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                  (GET "/login" [] "Here is our login page.")
                                  (friend_group/friend-group req)))
           (GET "/friend-group/invites" req
                (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                  (GET "/login" [] "Here is our login page.")
                                  (friend_group/see-friend-group-invites req)))
           (POST "/friend-group/remove-player" req
                (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                  (GET "/login" [] "Here is our login page.")
                                  (friend_group/remove-player-from-friend-group req)))

           (POST "/friend-group/accept-invite" req
                 (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                   (GET "/login" [] "Here is our login page.")
                                   (friend_group/accept-invite req)))

           (POST "/friend-group/reject-invite" req
                 (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                   (GET "/login" [] "Here is our login page.")
                                   (friend_group/reject-invite req)))

           (POST "/friend-group/update" req
                 (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                   (GET "/login" [] "Here is our login page.")
                                   (friend_group/update-friend-group req)))
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
                (game/player req)))
           (POST "/players" [& params]
                 (mud_user/make-player params))
           (POST "/friend-group" req
                 (friend_group/make-friend-group req))
           (POST "/actions" req
                 (friend/authorize #{::user} "This page can only be seen by authenticated users."
                                   (GET "/login" [] "Here is our login page.")
                                   (game/action req)))
           (route/resources "/")
           (route/not-found "<h1>Page not found</h1>"))

(defn find-user-and-role [usr]
  (let [usr-from-db (models/find-by-username (:username usr))]
    (if usr-from-db
      {(:username usr) (assoc usr-from-db :roles #{::user})}
      {})))

(def app (->
  (handler/site
    (friend/authenticate app-routes {
                                     :login-uri     "/login"
                                     :credential-fn #(creds/bcrypt-credential-fn (find-user-and-role %) %)
                                     :workflows     [(workflows/interactive-form)]})
    (wrap-flash app-routes)
    (wrap-keyword-params app-routes)
    (wrap-params app-routes))
  reload/wrap-reload
  (wrap-cors
    :access-control-allow-origin (re-pattern (env :cross-domain-url)))))


(defn -main [& args]
  (let [port (Integer/parseInt
               (or (System/getenv "PORT") "8080"))]
    (run-server app {:port port :join? false})))


