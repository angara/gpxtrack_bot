
(ns user
  (:require
    [clojure.tools.namespace.repl :as repl]
    [mount.core :as mount]
    ;
    [mlib.config :refer [conf]]
    [mlib.util :refer [edn-read edn-resource]]
    ;
    [gtb.app.poll :as poll]))
;=

(declare start)
(declare start-conf)
(declare stop)
(declare reset)
(declare restart)

(comment
  (start-conf)
  (stop)

  (reset)
 
  (restart)

  ,)

(defn reset []
  (repl/refresh :after 'user/restart))
;

(defn restart []
  (stop)
  (start))
;

(defn get-worker []
  poll/worker)
;

(defn configs []
  [ (edn-resource "config.edn")
    (edn-read "../conf/dev.edn")])
;

(defn start-conf[]
  (mount/stop)
  (->
    (mount/only [#'conf])
    (mount/with-args (configs))
    (mount/start)))
;

(comment
  (start-conf)

  conf

  (edn-read "../conf/dev.edn")
  ,)

(defn start []
  (prn "start with configs")
  (mount/start-with-args (configs)))
;

(defn stop []
  (prn "stop")
  (mount/stop))
;

;;.


