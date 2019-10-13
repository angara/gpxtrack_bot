(ns gtb.lib.tags
  (:refer-clojure :exclude [replace])
  (:require
    [clojure.string :refer [trim lower-case replace]]))
;=

(def SEASONS 
  ["зима" "весна" "лето" "осень"])
;=

(def SEASON_SET
  (set SEASONS))
;=

(def ACTIVITY
  ["альп" "авто" "вело" "мото" "лыжи" "скитур" "пеший" "кайт" "каяк" "коньки"])
;=

(def ACTIVITY_SET
  (set ACTIVITY))
;=

;; alp climb hike enduro bike ski skitour kite kayak iceskate


(defn norm-tag [s]
  (when (string? s)
    (-> (trim s)
      (lower-case)
      (replace #"^#+" "")
      (replace #"[^a-zа-яё0-9_]" "")
      (replace #"__+" "_")
      (as-> x
        (when (re-find #"[a-zа-яё]" x)
          x)))))
;;

(comment

  (norm-tag "  ##Qwsf  ") ;; => "qwsf"
  (norm-tag "__")         ;; => nil
  (norm-tag "...Ё")       ;; => ё

  ,)

;;.




;; https://support.strava.com/hc/en-us/articles/216919407-Supported-Activity-Types-on-Strava
