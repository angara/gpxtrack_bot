
(ns gtb.gpx.core
  (:require
    [clojure.java.io  :refer [input-stream]]
    [clojure.xml      :refer [parse]]))

(defn parse-bytes [bytes]
  (let [
        gpx (parse (input-stream bytes))]
    ;; (prn "gpx:" gpx)
    gpx))
;;

;;.


