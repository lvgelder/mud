(ns mud.views
  (:require [hiccup.page :refer [html5 include-js include-css]]
            [hiccup.form :refer [form-to text-field submit-button text-area]]
            [ring.util.response :as response]
            [mud.models :as models]
            ))


(defn index []
  (response/redirect "/entrance"))

;
(defn base-page [title & body] ;;(1)
  (html5
    [:head
     (include-css "/css/bootstrap.min.css") ;;(2)
     (include-css "/css/mud.css")
     [:title title]]
    [:body
     [:div {:class "navbar navbar-inverse"}
      [:div {:class :navbar-inner}
       [:a {:class :brand :href "/"} "SUD!"]
       [:form {:class "navbar-form pull-right"}
        [:input {:type :text :class :search-query :placeholder :Search}]]]]

     [:div.container (seq body)]])) ;;(3)

(defn entrance []
  (base-page
    "Welcome to Sud (a single user dungeon)"
    [:div.row.admin-bar
     [:a {:href "/player/new"}
      "Create a new hero"]]
    [:h1 "Player List"]
    [:ol
     (for [p (models/all-players)] ;;(4)
       [:li [:a {:href (str "/player/" (:id p))} (:name p)]])]))

(defn new-player []
  (base-page
    "New Hero"

    [:h1 "Create a new hero"]

    (form-to
      {:class :form-horizontal}
      [:post "/players"]
      (text-field :name)
      (text-field :description)
      (submit-button {:class "btn btn-primary"} "Create Hero"))))

(defn make-player [params]
  (models/create-player params)
  (response/redirect-after-post "/entrance"))