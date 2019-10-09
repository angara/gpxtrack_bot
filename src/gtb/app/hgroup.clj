
(ns gtb.app.hgroup
  (:require
    [mlib.logger    :refer  [debug warn]]
    [mlib.telegram  :refer  [send-text]]
    ;
    [gtb.app.cfg    :as     cfg]
    [gtb.app.file   :refer  [save-gpx-file]]
    [gtb.app.track  :refer  [describe]]))
;=


(defn group-message [message is-gpx]
  (debug "group-message:" message)
  (cond
    ;
    is-gpx
    (when-let [track (save-gpx-file message true)]
      (send-text
        (-> message :chat :id)
        (describe track)
        cfg/tg))
    ;
    :else
    false))
;;

;;.
