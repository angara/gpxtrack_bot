
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

(defn describe [track]
  (let [{:keys [id info file orig]} track]
    (str 
      "#track <b>" id "</b> 🔸 " (hesc (:title info)) "\n"
      (status-icon track)
      (when-let [u (-> orig :telegram :from :username)]
        (str "Загрузил: @" u "\n"))
      "\n"
      "download: " (:base-url cfg/app) (:path file)
      "\n")))
;;


;;.
