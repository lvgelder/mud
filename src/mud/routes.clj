(ns mud.routes
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [mud.views :as views]
            [mud.models :as models]))

;
(defroutes app-routes
           (GET "/" [] ;;(1)
                (views/index))
           (GET "/entrance" []
                (views/entrance))
           (GET "/player/new" []
                (views/new-player))
           (GET "/player/:id" [id] ;;(2)
                (views/player id))
           (POST "/players" [& params]
                 (views/make-player params))
           )

(def app
  (-> app-routes ;;(4)
      (wrap-resource "public") ;;(5)
      wrap-keyword-params
      wrap-params))
;
