
(ns gtb.app.hgroup
  (:require
    [mlib.logger    :refer  [debug warn]]
    [mlib.telegram  :refer  [send-message send-text]]
    ;
    [gtb.app.cfg    :as     cfg]
    [gtb.app.file   :refer  [save-gpx-file]]
    [gtb.app.track  :refer  [describe inline-keyboard]]))
;=


(defn handle-group-gpx [chat-id message]
  (let [  
          {:keys [error track old-track]} 
          (save-gpx-file message true)]
    ;
    (cond
      error
      (do
        (debug "duplicate: " (:id old-track))
        (when old-track
          (send-text chat-id
            (str "Трек уже загружен: #" (:id old-track))
            cfg/tg)))            
      ;
      track
      (do
        (debug "track.public:" track)
        (send-message
          chat-id
          { :text (describe track)
            :parse_mode "HTML"
            :reply_markup (inline-keyboard track)}
          cfg/tg)))))
;;


(defn group-message [message is-gpx]
  (debug "group-message:" message)
  (let [chat-id (-> message :chat :id)]
    (cond
      ;
      is-gpx
      (handle-group-gpx chat-id message)
      ;
      :else
      false)))
;;

;;.
