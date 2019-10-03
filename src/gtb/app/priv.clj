
(ns gtb.app.priv
  (:require
    [mlib.logger  :refer [debug]]
    ;
    [gtb.const    :refer [MIME_GPX]]
    [gtb.app.file :refer [save-gpx-file]]))
;=


(defn priv-message [message]
  (debug "priv-message:" message)
  (cond
    ;
    (= MIME_GPX (-> message :document :mime_type))
    (do
      (save-gpx-file message))
      ;; (reply-gpx)
    ;
    :else 
    false))
;;

;;.

