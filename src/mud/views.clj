(ns mud.views
  (:require [hiccup.page :refer [html5 include-js include-css]]
            [hiccup.form :refer [form-to text-field submit-button text-area hidden-field]]
            [ring.util.response :as response]
            [mud.models :as models]
            [mud.brain :as brain]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            ))


(def signup-form
  [:div {:class "row"}
   [:div {:class "columns small-12"}
    [:h3 "Sign up "
     [:small "(Any user/pass combination will do, as you are creating a new account or profile.)"]]
    [:div {:class "row"}
     [:form {:method "POST" :action "/signup" :class "columns small-4"}
      [:div "Username" [:input {:type "text" :name "username" :required "required"}]]
      [:div "Password" [:input {:type "password" :name "password" :required "required"}]]
      [:div "Confirm" [:input {:type "password" :name "confirm" :required "required"}]]
      [:div
       [:input {:type "submit" :class "button" :value "Sign up"}]
       ]]]]])

(def login-form
  [:div {:class "row"}
   [:div {:class "columns small-12"}
    [:a {:href "/signup"}
     "Create a new user"]
    [:h3 "Login"]
    [:div {:class "row"}
     [:form {:method "POST" :action "login" :class "columns small-4"}
      [:div "Username" [:input {:type "text" :name "username"}]]
      [:div "Password" [:input {:type "password" :name "password"}]]
      [:div [:input {:type "submit" :class "button" :value "Login"}]]]]]])

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

(defn login []
  (base-page "Login"
             [:div
              login-form]
             ))

(defn sign-up []
  (base-page "Create user"
             [:div
              signup-form]
             ))

(defn entrance [req]
  (base-page
    "Welcome to Sud (a single user dungeon)"
    [:div.row.admin-bar
     [:a {:href "/player/new"}
      "Create a new hero"]]
    [:p (if-let [identity (friend/identity req)]
          (apply str "Logged in, with these roles: "
                 (-> identity friend/current-authentication :roles pr-str))
          "anonymous user")]

    [:h1 "Player List"]
    [:ol
     (for [p (models/all-players)] ;;(4)
       [:li [:a {:href (str "/player/" (:id p))} (:name p)]])]))

(defn new-player []
  (base-page
    "New Hero"


    (form-to
      {:class "form-horizontal"}
      [:post "/players"]
      [:legend "Create a new hero"]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Name"]
       [:div {:class "controls"}
        (text-field :name)]]
      [:div {:class "control-group"}
       [:label {:class "control-label"} "Description"]
       [:div {:class "controls"}
       (text-field :description)]]
      [:div {:class "control-group"}
       [:div {:class "controls"}
       (submit-button {:class "btn btn-primary"} "Create Hero")]])))

(defn make-player [params]
  (models/create-player params)
  (let [pl (models/player-by-name (:name params))]
    (models/initialize-player-room (:id pl) 1)
    (models/initialize-player-weapon (:id pl))
    )
  (response/redirect-after-post "/entrance"))

(defn create-user [params]
  (models/create-user params)
  (let [usr (models/find-by-username (:username params))]
    (models/add-user-role (:id usr) 1)
    )
  (response/redirect-after-post "/entrance")
  )

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

(defn player [id-str]
  (let [id (read-string id-str) pl (models/player-by-id id) room (models/room-by-player-id id)]
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

