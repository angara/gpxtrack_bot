
(ns gtb.app.poll
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
    { :cfg        (get-in conf [CFG :telegram])
      :last-update 0}))
;;

(defn step [*state]
  (prn "state:" @*state)
  (let [{:keys [cfg last-update]} @*state
        ;
        {:keys [result error] :as rc} 
        (telegram/get-updates (:apikey cfg) (inc last-update) 1 cfg)
        ;
        ,]
    (debug "rc:" rc)
    (prn "rc:" rc)
    (if-let [update (first result)]
      (do
        (debug "update:" update)
        (swap! *state assoc :last-update (:update_id update)))
        ;; process update
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

  worker 

  (name telegram/E_RETRY_LIMIT)

  conf
  
  (init (atom {}))

  (let [*state (atom {})]
    (init *state)
    (step *state)
    (step *state))

  ,)
