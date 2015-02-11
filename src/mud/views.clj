(ns mud.views
  (:require [hiccup.page :refer [html5 include-js include-css]]
            [hiccup.form :refer [form-to text-field submit-button text-area hidden-field]]
            [ring.util.response :as response]
            [mud.models :as models]
            [mud.brain :as brain]
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
       ]]

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
      [:div
       "Name: "
       (text-field :name)
       ]
      [:div "Description:"
       (text-field :description)
       ]
      (submit-button {:class "btn btn-primary"} "Create Hero"))))

(defn make-player [params]
  (models/create-player params)
  (let [pl (models/player-by-name (:name params))]
    (models/initialize-player-room (:id pl) 1)
    )
  (response/redirect-after-post "/entrance"))

(defn player-page [pl room action]
  (base-page
    (str (:name pl) " - the hero")

    [:h1 (:name pl)]
    [:div
     (if (not (empty? (:treasure pl)))
       [:div  {:class "pull-right"}
        "Items"
        [:ul
         (for [item (:treasure pl)]
           [:li (:description item) ]
           )
         ]
        ]
       )
     (str (format "items %s" (count (:treasure pl))))

     ]
    [:div (str (format "monsters killed %s" (count (:monster pl))))]


    [:hr]

    [:p "You are in:"]
    [:p (:description room)]

    [:div
     (for [monster (:monster room)]
       [:p (str (format "You see a %s with %s" (:name monster) (:description monster))) ]
       )
     ]

    [:p action]

    (form-to
      {:class :form-horizontal}
      [:post "/actions"]
      [:div
       (text-field :action)
       (hidden-field :player_id (:id pl))
       (hidden-field :room_id (:id room))
       (submit-button {:class "btn btn-primary"} "What do you want to do?")
       ]
      )
    )
  )

(defn player [id]
  (let [pl (models/player-by-id id) room (models/room-by-player-id id)]
    (player-page pl room nil)
    )
  )

(defn action [params]
  (let [action (brain/action (:player_id params) (:action params) (:room_id params)) pl (models/player-by-id (:player_id params)) room (models/room-by-player-id (:player_id params))]
    (player-page pl room action)
    )
  )

