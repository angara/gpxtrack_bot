
(ns gtb.app.core
  (:require 
    [mount.core :refer [defstate]]
    ;
    [mlib.config :refer [conf]]
    [mlib.logger :refer [debug]]))
;=

(defstate worker
  :start
    (do
      (debug "conf:" conf)
      true))
;=

;;.
