(ns mud.chat
  (:require [mud.messages :as messages]
            [clojure.string :as str]
            [mud.models :as models]))

(defn send-message [message to-player-id message-to message-from]
  do
  (messages/messsage (format "<p>%s says %s</p>" message-from message) to-player-id)
  (format "<p>You said %s to %s.</p>" message message-to)
  )

(defn say [player-id action room-id]
  (let [message-parts (re-find #"say (.*) to (.*)" action)
        message (nth message-parts 1)
        to-name (nth message-parts 2)]
    (if (< (count message-parts) 3)
      "I don't know how to do that."
      (let [player-to-talk-to (models/player-by-name (str/trim to-name))
            current-player (models/player-by-id player-id)]
        (cond
          (not player-to-talk-to) (format "I don't know who %s is." to-name)
          (not (messages/currently-playing (:id player-to-talk-to))) (format "<p>%s is not there to talk to.</p>" to-name)
          (not (= room-id (:id (first (:room player-to-talk-to))))) (format "<p>%s is not there to talk to.</p>" to-name)
          :else (send-message message (:id player-to-talk-to) (str/trim to-name) (:name current-player))
          )))))

(defn list-players [players player-id]
    (let [players-without-me (filter #(not(= (:id %) player-id)) players)
          currently-playing (filter #(messages/currently-playing (:id %)) players-without-me)
          names (map #(:name %) currently-playing)
          join-word (if (> (count currently-playing) 1) "are" "is")]
      (if (not-empty currently-playing)
        (format "%s %s in the room with you." (str/join " and " names) join-word)
        "")))

(defn join-room [player room-id]
  (let [players-in-room (models/find-players-in-room room-id)
        players-without-me (filter #(not(= (:id %) (:id player))) players-in-room)
        currently-playing (filter #(messages/currently-playing (:id %)) players-without-me)]
    (doall (map #(messages/messsage (format "<p>%s has entered the room.</p>" (:name player)) (:id %)) currently-playing))))