
(ns gtb.app.file
  (:require
    [clojure.java.io  :as     io]
    [clojure.string   :as     str]
    [java-time        :as     jt]
    [mlib.crypto      :refer  [byte-array-hash-str]]
    [mlib.logger      :refer  [debug warn]]
    [mlib.telegram    :refer  [file-fetch] :as tg]
    ;
    [gtb.app.cfg      :as     cfg]
    [gtb.db.core      :refer  [inc-var TRACK_SEQ_VAR]]
    [gtb.gpx.core     :refer  [parse-bytes]]))
;=

(def FILE_SIZE_MAX (* 10 1024 1024))  ;; 10 Mb

(def HASH_FN "SHA-1")

(defn next-track-id []
  (str (inc-var TRACK_SEQ_VAR)))
;;


(defn- yyyymmdd []
  (-> 
    (jt/local-date)
    (jt/as :year :month-of-year :day-of-month)
    (as-> x
      (map #(format "%02d" %) x))))
;;

(defn write-data
  "write byte array to file, return track-path"
  [id ^:bytes data]
  (let [yymmdd    (yyyymmdd)
        base-dir  (-> cfg/files :base-dir)
        prefix    (-> cfg/app :storage :prefix) 
        file-name (str id ".gpx")
        file      (apply io/file (concat [base-dir prefix] yymmdd [file-name]))]
    ;
    (io/make-parents file)
    (with-open [out (io/output-stream file)]
      (.write out data))
    (str/join "/" (concat [prefix] yymmdd [file-name]))))
;;

(defn save-gpx-file [message]
  ; (debug "save-chat-file:" (:document message))

  (let [{{  file-id   :file_id 
            file-size :file_size 
            file-name :file_name} :document
          caption                 :caption} message]
    ;
    (if (> file-size FILE_SIZE_MAX)
      (do
        (warn "file too big:" file-size)
        false)
      ;;
      (let [{body :body :as fr} (file-fetch file-id cfg/tg)
            hash (byte-array-hash-str HASH_FN body)
            ; gpx  (parse-bytes body)
            id   (next-track-id)
            path (write-data id body)]
            ;
        (debug "file:" hash fr path)
        { :id         id 
          :path       path 
          :hash       hash 
          :file-name  file-name}))))
;;

;;,
