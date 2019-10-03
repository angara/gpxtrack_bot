
(ns gtb.app.core
  (:require
    [mount.core   :refer [defstate]]
    [mlib.config  :refer [conf]]
    [gtb.const    :refer [CFG]]))
;=


(defstate app-cfg
  :start
    (get conf CFG))
;=


(defstate tg-cfg
  :start
    (:telegram app-cfg))
;=
 
;;.
