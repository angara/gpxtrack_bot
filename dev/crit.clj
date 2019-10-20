
(ns crit
  (:require  
    [criterium.core :refer [quick-bench with-progress-reporting]]))
;=


(set! *warn-on-reflection*  true)
(set! *unchecked-math*      false)


(defn sq1 [^double x]
  (+ x (* (Math/sin x) (Math/sin x))))
;;

(defn sq2 [^double x]
  (+ x (Math/pow (Math/sin x) 2)))
;;

(defn sq3 [^double x]
  (let [y (Math/sin x)]
    (+ x (* y y))))
;;

(comment

  (do
    (with-progress-reporting 
      (quick-bench (sq1 1.) :verbose))
    (with-progress-reporting 
      (quick-bench (sq2 1.) :verbose))
    (with-progress-reporting 
      (quick-bench (sq3 1.) :verbose)))

  ,)

;;.

