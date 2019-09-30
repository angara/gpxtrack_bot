
(ns gtb.app.core
  (:require 
    [mount.core :refer [defstate]]
    ;
    [mlib.logger :refer [debug]]))
;=

(defstate worker
  :start
    true)
;=

;;.
