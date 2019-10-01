
(ns gtb.app.core
  (:require 
    [mount.core :refer [defstate]]
    ;
    [mlib.config :refer [conf]]
    [mlib.logger :refer [debug warn]]
    [mlib.thread :refer [start-loop stop-loop]]
    [mlib.telegram :as telegram]
    ;
    [gtb.const :refer [CFG]]))
;=

(defn init [*state]
  (reset! *state
    {:cfg (get-in conf [CFG :telegram])}))
;;

(defn step [*state]
  (let [{{apikey :apikey} :cfg} @*state
        {:keys [result error]} (telegram/get-updates apikey 1)]
    (if-let [update (first result)]
      (debug "update:" update)
      (do
        (warn "get-updates:" error)
        (Thread/sleep 5000)))))
;;

(defn cleanup [_state ex]
  (when ex
    (warn "worker.cleanup:" ex)))

;;

(defstate worker
  :start
    (start-loop init step cleanup)
  :stop
    (stop-loop worker))
;=

;;.


(comment

  (name telegram/E_RETRY_LIMIT)

  conf
  
  (binding [telegram/*opts* (-> conf :gpxtrack :telegram)]
    (telegram/get-updates
      (-> conf :gpxtrack :telegram :apikey)
      1))

  ,)
