
(ns gtb.main
  (:gen-class)
  (:require
    [clojure.string :refer [blank? split]]
    [mount.core     :as mount]
    ;;
    [mlib.config    :refer [conf]]
    [mlib.util      :refer [edn-read edn-resource]]
    [mlib.logger    :refer [debug info warn]]
    ;;
    [gtb.app.core :refer [worker]]))
;

(defn load-edn [file-name]
  (debug "load-edn:" file-name)
  (edn-read file-name))
;

(defn load-env-configs [env]
  (let [edns (->> (split (str env) #"\:") (remove blank?) seq)]
    (if-not edns
      (warn "load-env-configs:" "no configs in CONFIGS_EDN")
      (map load-edn edns))))
;

(declare app-start)
(mount/defstate app-start
  :start
    (info "started:" (:build conf)))
;

(defn -main []
  (and 
    worker 
    :suppress-linter-warning)
  
  (info "init...")
  (mount/start-with-args
    (concat
      [(edn-resource "config.edn") {:build (edn-resource "build.edn")}]
      (load-env-configs (System/getenv "CONFIG_EDN")))))

  ; (info "main started."))
  ;   (join worker)))
;;

;;.
