(ns mud.views
  (:require [hiccup.page :refer [html5 include-js include-css]]
            [hiccup.form :refer [form-to text-field submit-button text-area hidden-field password-field]]
            [ring.util.response :as response]
            [mud.models :as models]
            [mud.brain :as brain]
            [mud.validations :as valid]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            ))


(defn index []
  (response/redirect "/player"))

(defn base-page [title & body] ;;(1)
  (html5
    [:head
     (include-css "/css/bootstrap.min.css") ;;(2)
     (include-css "/css/mud.css")
     [:title title]]
    [:body
     [:div {:class "navbar navbar-inverse"}
      [:div {:class :navbar-inner}
       [:div [:a {:class :brand :href "/player"} "SUD!"]]
       [:div {:class "navbar-right"}
        [:a {:class "brand" :href "/logout"} "logout"]]
       ]
      ]

     [:div.container (seq body)]])) ;;(3)

(defn login [req]
  (base-page
    "Welcome to Sud (a single user dungeon)"
    [:div.row.admin-bar
     [:a {:href "/player/new"}
      "Create a new hero"]]
    [:div
     [:legend "Login"]
     [:form {:method "POST" :action "login" :class "form-horizontal"}
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Username"]
       [:div {:class "controls"}
        [:input {:type "text" :name "username"}]]]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Password"]
       [:div {:class "controls"}
        [:input {:type "password" :name "password"}]]]
      [:div {:class "control-group"}
       [:div {:class "controls"}
        (submit-button {:class "btn btn-primary"} "Login")]]]]
    [:div (:flash req)]
    ))

(defn new-player [req]
  (base-page
    "New Hero"
    (form-to
      {:class "form-horizontal"}
      [:post "/players"]
      [:legend "Create a new hero"]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Your Username"]
       [:div {:class "controls"}
        (text-field :username (:username (:form-vals (:flash req))))
       [:div
        (for [error (:username (:flash req))]
          [:div error ]
          )]]]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Your Password"]
       [:div {:class "controls"}
        (password-field :password (:password (:form-vals (:flash req))))
        [:div
         (for [error (:password (:flash req))]
           [:div error ]
           )]]]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Name of your Hero"]
       [:div {:class "controls"}
        (text-field :name (:name (:form-vals (:flash req))))
        [:div
         (for [error (:name (:flash req))]
           [:div error ]
           )]]]
      [:div {:class "control-group"}
       [:div {:class "controls"}
       (submit-button {:class "btn btn-primary"} "Create Hero")]])
    [:div (:flash req)]
    ))

(defn make-player [params]
  (let [err (valid/valid-user? params)]
    (if (not(empty? err))
      (assoc (response/redirect "/player/new") :flash (assoc err :form-vals {:username (:username params)
                                                                             :password (:password params)
                                                                             :name (:name params)}))
      (assoc (response/redirect "/login") :flash "success")
      )
    ))
  ;(models/create-player params)
  ;(models/create-user params)
  ;(let [pl (models/player-by-name (:name params))
  ;      usr (models/find-by-username (:username params))]
  ;  (models/initialize-player-room (:id pl) 1)
  ;  (models/initialize-player-weapon (:id pl))
  ;  (models/add-user-role (:id usr) 1)
  ;  (models/add-user-player (:id usr) (:id pl))
  ;  )
;(assoc (response/redirect-after-post "/login") :flash "New player created successfully! Please login with your new credentials."))

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
    [:div (str (format "hit points: %s" (:hit_points pl)))]

    [:hr]

    (if action
      [:div
        [:p action]
        [:hr]
        ]
      )


    [:p "You are in:"]
    [:p (:description room)]

    [:div
     (for [monster (:monster room)]
       [:p (str (format "You see a %s with %s" (:name monster) (:description monster))) ]
       )
     ]

    (form-to
      {:class "form-horizontal"}
      [:post "/actions"]
      [:div {:class "form-group"}
       (text-field {:class "input-large"} :action)
       (hidden-field :player_id (:id pl))
       (hidden-field :room_id (:id room))
       [:span {:style "padding: 10px"} (submit-button {:class "btn btn-primary pad-left"} "What do you want to do?")]
       ]
      )
    )
  )

(defn player [req]
  (let [identity (friend/identity req)
        usr (models/find-by-username (:current identity))
        pl (models/find-player-by-username (:id usr))
        room (models/room-by-player-id (:id pl))]
    (player-page pl room nil)
    )
  )

(defn action [params]
  (let [player-id (read-string (:player_id params))
        room-id (read-string (:room_id params))
        action (brain/action player-id (:action params) room-id)
        pl (models/player-by-id player-id)
        room (models/room-by-player-id player-id)]
    (player-page pl room action)
    )
  )

