
(ns gtb.app.hpriv
  (:require
    [mlib.logger    :refer  [debug]]
    [mlib.telegram  :refer  [send-text hesc]]
    ;
    [gtb.app.cfg    :as     cfg]
    [gtb.db.core    :refer  [track-by-id]]
    [gtb.app.file   :refer  
      [save-gpx-file E_FILE_FORMAT E_FILE_TOO_BIG]]
    [gtb.app.cmd    :refer
      [ split-cmd 
        cmd-help cmd-list
        CMD_HELP CMD_START CMD_TRACK CMD_LIST]]
    [gtb.app.track  :as     trk]))
;=


(defn cmd-track [chat-id [id]]
  (debug "t:" id)
  (if (empty? id)
    (send-text chat-id 
      "Необходимо указать идентификатор трека.\nПример: <code>/track 123</code>" 
      cfg/tg)
    (if-let [trk (track-by-id id)]
      (send-text chat-id (trk/describe trk) cfg/tg)
      (send-text chat-id 
        (str "Трек <b>" (hesc id) "</b> не найден❗️")
        cfg/tg))))
;;

(defn cmd-start [chat-id [cmd id]]
  (if (= cmd "track")
    (cmd-track chat-id [id])
    (cmd-help  chat-id)))
;;

(defn handle-gpx [chat-id message]
  (let [  {:keys [error track]} 
          (save-gpx-file message true)]
    ;
    (cond 
      ;
      (= error E_FILE_FORMAT)
      (send-text chat-id "Некорректный формат файла❗️" cfg/tg)
      ;
      (= error E_FILE_TOO_BIG)
      (send-text chat-id "Слишком большой файл❗️" cfg/tg)
      ;
      track
      (do
        (debug "track.private:" track)
        (send-text
          chat-id
          (trk/describe track)
          cfg/tg)))))
;;

(defn priv-message [message is-gpx]
  (debug "priv-message:" message)
  (let [{{chat-id :id} :chat text :text} message
        [cmd & args] (split-cmd text)]
    ;
    (cond
      ;
      is-gpx
      (handle-gpx chat-id message)
      ;
      (= CMD_HELP cmd)
      (cmd-help chat-id)
      ;
      (= CMD_START cmd)
      (cmd-start chat-id args)
      ;
      (= CMD_TRACK cmd)
      (cmd-track chat-id args)
      ;
      (= CMD_LIST cmd)
      (cmd-list chat-id args)
      ;
      :else 
      (send-text chat-id 
        "ℹ️ Для получения справки введите /help" 
        cfg/tg))))
;;

;;.
