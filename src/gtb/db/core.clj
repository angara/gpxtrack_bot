
(ns gtb.db.core
  (:require
    ; [clojure.spec.alpha :as s]
    ; [java-time :as time]
    [monger.collection :as mc]
    [monger.query :as mq]
    ;
    [mount.core   :refer [defstate]]
    [mlib.mongo   :refer [connect disconnect new_id id_id]]
    [mlib.config  :refer [conf]]
    [mlib.util :refer [now-ms]]))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

;; collections

(def TRACK_COLL     :track)
(def TRACK_PNT_COLL :track_pnt)
(def TRACK_VAR_COLL :track_var)

(def TRACK_SEQ_VAR  "track_id")


;; limits 

(def FETCH_LIMIT      10000)
(def FETCH_LIMIT_MAX  100000)

; (def RE_OID #"[0-9a-fA-F]+")
; (s/def ::oid-spec (s/and string? #(re-matches RE_OID %)))
  
;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn indexes [conn]
  (let [db (:db conn)]
    ;
    (mc/create-index db TRACK_COLL      (array-map :user_id  1))
    ;
    (mc/create-index db TRACK_PNT_COLL  (array-map :track_id 1))
    (mc/create-index db TRACK_PNT_COLL  (array-map :coord   "2dsphere")))
    ;
  conn)
;;

(defn init-vars [conn]
  (let [db (:db conn)]
    (mc/find-and-modify db TRACK_VAR_COLL
      {:_id   TRACK_SEQ_VAR}
      {:$inc  {:val 0}}
      {:upsert true}))
  conn)
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defstate mdb
  :start
    (-> conf      
      (get-in [:mdb :angara :url])
      (connect)
      (indexes)
      (init-vars))
  :stop
    (disconnect mdb))
;=

(defn dbc []
  (:db mdb))
;;

(defn new-id []
  (str (new_id)))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn norm-offset [offset]
  (max 0 (or offset 0)))
;;

(defn norm-limit [limit]
  (min FETCH_LIMIT_MAX (or limit FETCH_LIMIT))) 
;;

(defn coll-find [coll {:keys [query sort fields offset limit]}]
  (-> (dbc)
    (mq/with-collection (name coll)
      (mq/find    (or query  {}))
      (mq/sort    (or sort   []))
      (mq/fields  (or fields []))
      (mq/skip    (norm-offset offset))
      (mq/limit   (norm-limit  limit)))
    (as-> x
      (map id_id x))))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn get-var [name]
  (-> (dbc)
    (mc/find-map-by-id TRACK_VAR_COLL name)
    (:val)))
;;

(defn set-var [name value & [upsert]]
  (-> (dbc)
    (mc/update TRACK_VAR_COLL 
      {:_id   name}
      {:$set  {:val value}}
      {:upsert (boolean upsert)})
    (.getN)
    (= 1)))
;;

(defn inc-var [name & [n]]
  (-> (dbc)
    (mc/find-and-modify TRACK_VAR_COLL
      {:_id   name}
      {:$inc  {:val (or n 1)}}
      {:return-new true})
    (:val)))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn create-track [id data]
  (prn "data:" data)
  (-> (dbc)
    (mc/insert-and-return TRACK_COLL
      (assoc data
        :ct (now-ms)
        :ts (now-ms)
        :_id id))
    (id_id)))
;;

(defn update-track [id data]
  (-> (dbc)
    (mc/update TRACK_COLL
      {:_id id}
      {:$set (assoc data {:ts (now-ms)})}
      {:upsert false})
    (.getN)
    (= 1)))
;;

(defn track-list [])
;;;;;;; XXX: implement

(defn track-by-id [id]
  (-> (dbc)
    (mc/find-one-as-map TRACK_COLL {:_id id})
    (id_id)))
;;

(defn track-by-hash [hash]
  (-> (dbc)
    (mc/find-one-as-map TRACK_COLL {:hash hash})
    (id_id)))
;;

;;.
