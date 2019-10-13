(ns gtb.app.cmd
  (:require
    [clojure.string :refer [trim split lower-case join]]
    ;
    [mlib.config    :refer  [conf]]
    [mlib.telegram  :refer  [send-text hesc]]
    [mlib.util      :refer  [to-int]]
    [gtb.app.cfg    :as     cfg]
    [gtb.db.core    :as     db]
    [gtb.app.track  :refer  [track-orig-name]]))
;=

(def ^:const CMD_HELP   "/help")
(def ^:const CMD_START  "/start")
(def ^:const CMD_TRACK  "/track")
(def ^:const CMD_LIST   "/list")

(def LIST_PAGE_SIZE  5)

(defn split-cmd [s]
  (when-let [s (and (string? s) (trim s))]
    (when (= \/ (first s))
      (->
        (split s #"[_ ]" 100)
        (update-in [0] lower-case)))))
;;


(defn cmd-help [chat-id]
  (send-text chat-id
    (str
      "📍<b>GPX Track bot</b>\n"
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
      "🛠 Команды:\n"
      " - /list - список треков\n"
      " - /track NNN - информация о треке\n"
      ;" * /search ... - поиск \n"
      "\n"
      "Обсуждение работы бота и пожелания по разработке - @gpxtrack_chat")
    cfg/tg))
;;

(defn format-list-item [track]
  (str "🚩 /track_" (:id track) "\n"
    (hesc 
      (or (:title track))
      (-> track :info :title))    ;; XXX: deprecated
    "\n"
    "Загрузил: " (track-orig-name track) "\n"
    "Скачать: " (:base-url cfg/app) (-> track :file :path) 
    "\n"))
;;

(defn cmd-list [chat-id [offset]]
  (let [ofs (max 0 (to-int offset 0))
        trs (db/track-list ofs LIST_PAGE_SIZE)
        text
            (->> trs
              (map format-list-item)
              (join "\n"))
        text 
            (if (>= (count trs) LIST_PAGE_SIZE)
              (str text "\nЕще... /list_" (+ ofs LIST_PAGE_SIZE))
              text)]
    ;
    (send-text chat-id text cfg/tg)))
;;
          
;;.
