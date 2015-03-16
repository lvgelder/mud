(ns mud.chat (:require [mud.messages :as messages]))

(defn say [player-id action room-id]
  (messages/messsage action 3)
  )