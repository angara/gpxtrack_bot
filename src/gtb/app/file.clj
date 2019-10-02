
(ns gtb.app.file
  (:require
    [mlib.logger :refer [debug warn]]))
;=

(defn save-chat-file [message]
  (debug "save-chat-file:" (:document message)))
;;

;;,
 