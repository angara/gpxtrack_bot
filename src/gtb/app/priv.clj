
(ns gtb.app.priv
  (:require
    [mlib.logger  :refer [debug]]
    ;
    [gtb.const    :refer [MIME_GPX]]
    [gtb.app.file :refer [save-chat-file]]))
;=


(defn priv-message [message]
  (debug "priv-message:" message)
  (cond
    ;
    (= MIME_GPX (-> message :document :mime_type))
    (save-chat-file message)
    ;
    :else 
    false))
;;

;;.

