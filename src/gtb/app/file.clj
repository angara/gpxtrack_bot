
(ns gtb.app.file
  (:require
    [mlib.logger :refer [debug warn]]
    ;
    [gtb.db.core :refer [inc-var TRACK_SEQ_VAR]]))
;=

(defn next-track-id []
  (str (inc-var TRACK_SEQ_VAR)))
;;



(defn save-gpx-file [message]
  (debug "save-chat-file:" (:document message)))
  ;; get bytes
  ;; parse gpx
  ;; calc hash
  ;; save file (seq++)
;;

;;,


(comment

  (next-track-id)

  ,)
