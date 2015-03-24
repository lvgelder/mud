(ns mud.views
  (:require [hiccup.page :refer [html5 include-js include-css]]
            [hiccup.element :refer [javascript-tag]]
            [hiccup.form :refer [form-to text-field submit-button text-area hidden-field password-field]]
            [ring.util.response :as response]
            [mud.models :as models]
            [environ.core :refer [env]]))


(defn index []
  (response/redirect "/player"))

(defn base-page [title & body]
  (html5
    [:head
     (include-css "/css/bootstrap.min.css")
     (include-css "/css/mud.css")
     (include-js "/js/jquery-1.11.2.min.js")
     [:title title]]
    [:body
     [:div {:class "navbar navbar-inverse"}
      [:div {:class :navbar-inner}
       [:div [:a {:class :brand :href "/player"} "Play!"]]
       [:div {:class "navbar-right"}
        [:a {:class "brand" :href "/logout"} "logout"]]
       [:div
        [:a {:class "brand" :href "/friend-group"} "friend group"]]
       ]
      ]

     [:div.container (seq body)]]))

(defn login [req]
  (base-page
    "Welcome to Sud (a single user dungeon)"
    [:div.row.admin-bar
     [:a {:href "/player/single/new"}
      "Single Player!"]
     [:a {:href "/player/multi/new"}
      "Multi-player!"]]
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
    [:div (:flash req)]))

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
          [:div {:class "text-error"} error ]
          )]]]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Your Password"]
       [:div {:class "controls"}
        (password-field :password (:password (:form-vals (:flash req))))
        [:div
         (for [error (:password (:flash req))]
           [:div {:class "text-error"} error ]
           )]]]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Name of your Hero"]
       [:div {:class "controls"}
        (text-field :name (:name (:form-vals (:flash req))))
        [:div
         (for [error (:name (:flash req))]
           [:div {:class "text-error"} error ]
           )]]]
      [:div {:class "control-group"}
       [:div {:class "controls"}
       (submit-button {:class "btn btn-primary"} "Create Hero")]])
    [:div (:flash req)]))

(defn new-friend-group [req]
  (base-page
    "New Friend Group"
    (form-to
      {:class "form"}
      [:post "/friend-group"]
      [:legend "Create a friend group"]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Name of your group"]
       [:div {:class "controls"}
        (text-field :name (:name (:form-vals (:flash req))))
        [:div
         (for [error (:name (:flash req))]
           [:div {:class "text-error"} error ]
           )]]]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Heroes to add to your group. This can be a comma-separated list. eg 'buffy,boffy'. Note that you will be added automatically, you do not need to add your own hero name."]
       [:div {:class "controls"}
        (text-field :playernames (:playernames (:form-vals (:flash req))))
        [:div
         (for [error (:playernames (:flash req))]
           [:div {:class "text-error"} error ]
           )]]]
      [:div {:class "control-group"}
       [:div {:class "controls"}
        (submit-button {:class "btn btn-primary"} "Create Friend Group")]])
    [:div (:flash req)]))

(defn edit-friend-group [req]
  (base-page
    "Edit Friend Group"
    (form-to
      {:class "form"}
      [:post "/friend-group/update"]
      [:legend "Edit friend group"]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Name of your group"]
       [:div {:class "controls"}
        (:name (:friend_group (:flash req)))]]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Heroes to add to your group. This can be a comma-separated list. eg 'buffy,boffy'. Note that you will be added automatically, you do not need to add your own hero name."]
       [:div {:class "controls"}
        (text-field :playernames (:playernames (:form-vals :flash req)))
        [:div
         (for [error (:playernames (:flash req))]
           [:div {:class "text-error"} error ]
           )]]]
      [:div {:class "control-group"}
       [:div {:class "controls"}
        (hidden-field :friend_group_id (:id (:friend_group (:flash req))))
        (submit-button {:class "btn btn-primary"} "Update Friend Group")]])

    [:div {:class "control-group"}
     "Heroes to remove from your friend group"
     (for [player (models/players-by-friend-group (:id (:friend_group (:flash req))))]
       [:div
        (:name player)
        (form-to
          {:class "form"}
          [:post "/friend-group/remove-player"]
          (hidden-field :player_id (:id player))
          (hidden-field :friend_group_id (:id (:friend_group (:flash req))))
          (submit-button {:class "btn btn-primary btn-xs"} "Remove")
          )
        ]
       )
     ]
    ))

(defn player-page [pl room action]
  (base-page
    (str (:name pl) " - the hero")

    [:javascript-tag (format "<script>var websocketUrl = '%s';</script>" (env :websocket-url)) ]
    (include-js "/js/messages.js")

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

    [:div {:class "message"}
       [:p action]]

    [:hr]

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
       [:span {:style "padding: 10px"} (submit-button {:class "btn btn-primary pad-left"} "What do you want to do?")]
       ])))

