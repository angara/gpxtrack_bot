
(ns gtb.app.priv
  (:require
    [mlib.logger    :refer [debug]]
    [mlib.telegram  :refer [send-message]]
    ;
    [gtb.const      :refer [MIME_GPX]]
    [gtb.app.cfg    :as    cfg]
    [gtb.app.file   :refer [save-gpx-file]]
    [gtb.app.track  :as    trk]))
;=


(defn priv-message [message]
  (debug "priv-message:" message)
  (cond
    ;
    (= MIME_GPX (-> message :document :mime_type))
    (when-let [track (save-gpx-file message)]
      (debug "track.private:" track)
      (send-message
        (-> message :chat :id)
        { :text (trk/describe track)
          :parse_mode "HTML"}
        cfg/tg))
    ;
    :else 
    false))
;;

;;.

