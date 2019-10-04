
(ns gtb.app.track
  (:require
    [mlib.telegram :refer [hesc]]
    ;
    [gtb.app.cfg   :as    cfg]))
;=

(defn describe [track]
  (str 
    "<b>Track" (:id track) "</b>"
    "\n"
    (hesc (:file-name track)) 
    "\n"
    "gpx: " (:base-url cfg/app) (:path track)
    "\n"))
;;

;;.
