
(ns gtb.app.track
  (:require
    [mlib.telegram  :refer [hesc]]
    ;
    [gtb.const      :refer [TRACK_STATUS_PRIVATE TRACK_STATUS_PUBLIC]]
    [gtb.app.cfg    :as     cfg]))
    
;=

(defn status-icon [track]
  (condp = (:status track)
    TRACK_STATUS_PRIVATE "⚠️ Приватный статус\n"
    TRACK_STATUS_PUBLIC  "🌐 Публично доступен\n"
    nil))
;;

(defn track-orig-name [track]
  (let [{:keys [username first_name last_name]} (-> track :orig :telegram)]
    (str 
      (when username (str "@" username " "))
      first_name " " last_name)))
;;

(defn describe [track]
  (let [{:keys [id info file _orig title _descr]} track]
    (str 
      "#track <b>" id "</b> 🔸 " (hesc (or title (:title info))) "\n"
      (status-icon track)
      "Загрузил: " (track-orig-name track) "\n"
      "download: " (:base-url cfg/app) (:path file)
      "\n")))
;;

(defn track-map-url [id]
  (str (-> cfg/app :map :track-url) id))
;;

(defn tg-track-url [id]
  (str "tg://resolve?domain=" (:botname cfg/tg) "&amp;start=track_" id))
;;

(defn inline-keyboard [track]
  {:inline_keyboard 
    [[
      ;{:text "🌐 На карте" :url (track-map-url (:id track))}
      {:text "⚙️ Подробнее" :url (tg-track-url  (:id track))}]]})
;;

;;.
