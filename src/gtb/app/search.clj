(ns gtb.app.search
  (:require
    [gtb.const    :refer [TRACK_STATUS_PRIVATE TRACK_STATUS_PUBLIC]]
    [gtb.db.core  :refer 
      [coll-find find-substring TRACK_COLL]]))
;=

;; (defn lookup [])

(defn search-title [qs offset limit]
  (let [query
        { :status {:$in [TRACK_STATUS_PRIVATE TRACK_STATUS_PUBLIC]}
          :$or [
                (find-substring "title"      qs)
                (find-substring "info.title" qs)]}]   ;; XXX: deprecated, status {:$in []}
    ;
    (coll-find TRACK_COLL
      {
        :query  query
        :sort   {:ts -1}    
        :offset offset
        :limit  limit})))
;;

(comment
 
  (search-title "test" 0 10)

  ,)

;;.
