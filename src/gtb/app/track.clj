
(ns gtb.app.track
  (:require
    [mlib.telegram  :refer [hesc]]
    ;
    [gtb.app.cfg    :as     cfg]))
;=


(defn describe [track]
  (let [{:keys [id info file orig]} track]
    (str 
      "#track <b>" id "</b> - "
      (hesc (:title info)) 
      "\n"
      (when-let [u (-> orig :telegram :from :username)]
        (str "@" u "\n"))
      "link: " (:base-url cfg/app) (:path file)
      "\n")))
;;

; (defn create-track [id data]
;   (db/create-track id data))
; ;;

;;.
