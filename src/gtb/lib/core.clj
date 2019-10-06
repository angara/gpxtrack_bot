
(ns gtb.lib.core
  (:require
    [clojure.string :refer [starts-with? blank?]]))
;=

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn not-blank? [s]
  (and 
    (string? s) 
    (not (blank? s))))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;  telegram user-id conversion  ;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn tg->user-id [^long user-id]
  (str "tg:" user-id))
;;

(defn user-id->tg [^String user-id]
  (when (starts-with? user-id "tg:")
    (Integer/parseInt (subs user-id 3))))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

;;.
