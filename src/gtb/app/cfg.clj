
(ns gtb.app.cfg
  (:require
    [mount.core   :refer [defstate]]
    [mlib.config  :refer [conf]]
    [gtb.const    :refer [CFG]]))
;=


(defstate app
  :start
    (get conf CFG))
;=


(defstate tg
  :start
    (:telegram app))
;=
 
(defstate files
  :start
    (:files conf))
;=

;;.
