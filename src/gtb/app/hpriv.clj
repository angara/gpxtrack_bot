
(ns gtb.app.hpriv
  (:require
    [clojure.string :refer  [trim lower-case]]
    ;
    [mlib.config    :refer  [conf]]
    [mlib.logger    :refer  [debug]]
    [mlib.telegram  :refer  [send-text]]
    ;
    [gtb.app.cfg    :as     cfg]
    [gtb.app.file   :refer  [save-gpx-file]]
    [gtb.app.track  :as     trk]
    [gtb.lib.core   :refer  [not-blank?]]))
;=


(defn cmd-help [chat-id]
  (send-text chat-id
    (str
      "<b>GPX Track bot</b>\n"
      "version: " (-> conf :build :version) " " (-> conf :build :timestamp) "\n"
      "\n"
      "Бот умеет сохранять <b>.gpx</b> файлы из групповых или приватных чатов, "
      "публиковать ссылки на них."
      "\n"
      "\nВ разработке:\n"
      " - данные трека/сегментов\n"
      " - редактирование своих треков\n"
      " - поиск по названию, активности, времени года, территориям\n"
      " - отображение на карте"
      "\n\n"
      "Обсуждение работы бота и пожелания по разработке - @gpxtrack_chat")
    cfg/tg))
;;

(defn priv-message [message is-gpx]
  (debug "priv-message:" message)
  (let [text    (:text message)
        chat-id (-> message :chat :id)
        cmd     (when (not-blank? text)
                  (-> text (trim) (lower-case)))]
    (cond
      ;
      is-gpx
      (when-let [track (save-gpx-file message false)]
        (debug "track.private:" track)
        (send-text
          chat-id
          (trk/describe track)
          cfg/tg))
      ;
      (= "/help" cmd)
      (cmd-help chat-id)
      ;
      (= "/start" cmd)
      (cmd-help chat-id)
      ;
      :else 
      (send-text chat-id "Для получения справки введите /help" cfg/tg))))
;;

;;.
