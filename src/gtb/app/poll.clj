
(ns gtb.app.poll
  (:require 
    [mount.core :refer [defstate]]
    ;
    [mlib.logger :refer [debug warn]]
    [mlib.thread :refer [start-loop stop-loop]]
    [mlib.util   :refer [now-ms]]
    [mlib.telegram :as telegram]
    ;
    [gtb.app.cfg     :as cfg]
    [gtb.app.inbound :refer [handle-update]]))
;=

(def LOG_MARK_INTERVAL  (* 120 1000))  ;; 120 sec
(def ERROR_PAUSE        10000)  ;; 10 sec


(defn init [*state]
  (debug "worker.init")
  (swap! *state 
    assoc
      :last-update  0
      :last-log     0))
;;

(defn get-updates [last-update]
  (try
    (first (telegram/get-updates (inc last-update) 1 cfg/tg))
    (catch Exception ex
      (warn "get-updates:" ex)
      (Thread/sleep ERROR_PAUSE))))
;;
  
(defn step [*state]
  (let [{:keys [last-update]} @*state
        upd (get-updates last-update)]
    ;
    (when upd
      (swap! *state assoc :last-update (:update_id upd))
      (try
        (handle-update upd)
        (catch Exception ex
          (warn "worker.step catch:" upd ex))))
    ;
    (let [{:keys [last-log last-update]} @*state
          now (now-ms)]
      (when (< last-log (- now LOG_MARK_INTERVAL))
        (debug "last-update:" last-update)
        (swap! *state assoc :last-log now)))))
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
