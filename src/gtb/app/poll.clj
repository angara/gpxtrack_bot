
(ns gtb.app.poll
  (:require 
    [mount.core :refer [defstate]]
    ;
    [mlib.config :refer [conf]]
    [mlib.logger :refer [debug warn]]
    [mlib.thread :refer [start-loop stop-loop]]
    [mlib.util   :refer [now-ms]]
    [mlib.telegram :as telegram]
    ;
    [gtb.const :refer [CFG]]
    [gtb.app.inbound :refer [handle-update]]))
;=

(def LOG_MARK_INTERVAL  (* 120 1000))  ;; 120 sec
(def ERROR_PAUSE        10000)  ;; 10 sec


(defn init [*state]
  (debug "worker.init")
  (swap! *state 
    assoc
      :cfg (get-in conf [CFG :telegram])
      :last-update 0
      :last-log    0))
;;

(defn step [*state]
  (let [{:keys [cfg last-update]} @*state
        ;
        {:keys [result error]} 
        (telegram/get-updates (:apikey cfg) (inc last-update) 1 cfg)
        ;
        ,]
    ;
    (if error
      (do
        (warn "get-updates:" error)
        (Thread/sleep ERROR_PAUSE))
      (do
        (when-let [update (first result)]
          (swap! *state assoc :last-update (:update_id update))
          (try
            (handle-update update)
            (catch Exception ex
              (warn "worker.step catch:" update ex))))
        ;
        (let [{:keys [last-log last-update]} @*state
              now (now-ms)]
          (when (< last-log (- now LOG_MARK_INTERVAL))
            (debug "last-update:" last-update)
            (swap! *state assoc :last-log now)))))))
        ;
      ;
    ;


;;

(defn cleanup [_state ex]
  (debug "worker.cleanup")
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
