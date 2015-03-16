(ns mud.chat
  (:require [mud.messages :as messages]
            [clojure.string :as str]))

(defn say [player-id action room-id]
  (messages/messsage action 3)
  )

(defn msg [message player-id]
  (messages/messsage message player-id)
  )

(defn list-players [players player-id]
    (let [players-without-me (filter #(not(= (:id %) player-id)) players)
          currently-playing (filter #(messages/currently-playing (:id %)) players-without-me)
          names (map #(:name %) currently-playing)
          join-word (if (> (count currently-playing) 1) "are" "is")]
      (if (not-empty currently-playing)
        (format "%s %s in the room with you." (str/join " and " names) join-word)
        "")))