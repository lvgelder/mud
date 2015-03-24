(ns mud.views
  (:require [hiccup.page :refer [html5 include-js include-css]]
            [hiccup.element :refer [javascript-tag]]
            [hiccup.form :refer [form-to text-field submit-button text-area hidden-field password-field]]
            [ring.util.response :as response]
            [mud.models :as models]
            [mud.brain :as brain]
            [mud.validations :as valid]
            [mud.chat :as chat]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [mud.core :as core]
            [environ.core :refer [env]]
            [clojure.string :as str]))


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
       [:div [:a {:class :brand :href "/player"} "SUD!"]]
       [:div {:class "navbar-right"}
        [:a {:class "brand" :href "/logout"} "logout"]]
       ]
      ]

     [:div.container (seq body)]]))

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

(defn valid-playernames [playernames]
  (let [player-list (str/split playernames #",")]
    (map #(models/player-by-name (str/trim %)) player-list)))

(defn save-new-friend-group [req]
  (let [identity (friend/identity req)
        current-player (core/get-player-from-identity identity)
        params (:params req)
        other-players (valid-playernames (:playernames params))
        fr (models/create-friend-group params)
        friend-group (models/friend-group-by-name (:name params))]
    (models/add-player-to-friend-group (:id current-player) (:id friend-group))
    (doall (for [player other-players]  (models/add-player-to-friend-group (:id player)(:id friend-group))))
    (response/redirect "/player")))

(defn save-updated-friend-group [req]
  (let [params (:params req)
        other-players (valid-playernames (:playernames params))]
    (doall (for [player other-players]  (models/add-player-to-friend-group (:id player)(read-string (:friend_group_id params)))))
    (response/redirect "/friend-group")))


;split into comma separated
;also allow people to add and remove from group, so have group listing and editing page

(defn all-playernames-exist [playernames]
  (let [players (valid-playernames playernames)]
    (every? identity players)))

(defn make-friend-group [req]
  (let [params (:params req)
        err (valid/valid-friend-group? params)]
    (if (not(empty? err))
      (assoc (response/redirect "/friend-group/new") :flash (assoc err :form-vals {:playernames (:playernames params) :name (:name params)}))
      (if-not (all-playernames-exist (:playernames params))
        (assoc (response/redirect "/friend-group/new") :flash { :playernames ["Hero does not exist"] :form-vals {:playernames (:playernames params) :name (:name params)}} )
        (save-new-friend-group req)))))


(defn update-friend-group [req]
  (let [params (:params req)]
      (if-not (all-playernames-exist (:playernames params))
        (assoc (response/redirect "/friend-group") :flash { :playernames ["Hero does not exist"] :form-vals {:playernames (:playernames params) :name (:name params)}} )
        (save-updated-friend-group req))))

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
        [:span {:style "padding-right: 10px"} (:name player)]

        (form-to
          {:class "form"}
          [:post "/friend-group/remove-player"]
          (hidden-field :player_id (:id player))
          (hidden-field :friend_group_id (:id (:friend_group (:flash req))))
          (submit-button {:class "btn btn-primary"} "Remove")
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

(defn friend-group [req]
  (let [identity (friend/identity req)
        pl (core/get-player-from-identity identity)
        player (models/player-by-id (:id pl))
        friend-group-id (:id (first (:friend_group player)))]
  (if-not friend-group-id
    (new-friend-group req)
    (edit-friend-group (assoc req :flash {:friend_group (models/friend-group-by-id friend-group-id)})))))

(defn player [req]
  (let [identity (friend/identity req)
        player (core/get-player-from-identity identity)
        pl (models/player-by-id (:id player))
        room (models/room-by-player-id (:id player))
        other-players (chat/list-players pl (:id room))]
    (-> (response/response (player-page pl room (str (:flash req) other-players)))
        (response/header "X-Clacks-Overhead" "GNU Terry Pratchett"))))

(defn action [req]
  (let [identity (friend/identity req)
        player (core/get-player-from-identity identity)
        room (models/room-by-player-id (:id player))
        action (brain/action (:id player) (:action (:params req)) (:id room))]
    (assoc (response/redirect "/player") :flash action)))

; check player is part of this friend group before allowing this action
(defn remove-player-from-friend-group [req]
  (let [params (:params req)
        friend_group_id (read-string (:friend_group_id params))
        player_id (read-string (:player_id params))
        identity (friend/identity req)
        player (core/get-player-from-identity identity)
        players-in-friend-group (models/players-by-friend-group friend_group_id)]
    (if-not (core/contains-item-with-id players-in-friend-group player)
      (assoc (response/redirect "/friend-group") :flash "Action not permitted.")
      ( do
        (models/remove-player-from-friend-group player_id friend_group_id)
        (response/redirect "/friend-group")))))

