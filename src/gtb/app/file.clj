
(ns gtb.app.file
  (:require
    [clojure.java.io  :as     io]
    [clojure.string   :refer [join trim] 
                      :rename {join str-join}]
    [java-time        :as     jt]
    [mlib.crypto      :refer  [byte-array-hash-str]]
    [mlib.logger      :refer  [debug warn]]
    [mlib.telegram    :refer  [file-fetch] :as tg]
    ;
    [gtb.const        :refer 
      [ TRACK_STATUS_PUBLIC 
        TRACK_STATUS_PRIVATE 
        TRACK_TYPE_GPX
        TRACK_TITLE_LENGTH]]
    [gtb.app.cfg      :as     cfg]
    [gtb.db.core      :refer  [inc-var TRACK_SEQ_VAR create-track track-by-hash]]
    [gtb.lib.core     :refer  [not-blank? tg->user-id]]
    [gtb.lib.gpx      :refer  [parse-bytes]]))
;=


(def E_FILE_TOO_BIG   ::e-file-too-big)
(def E_FILE_EXISTS    ::e-file-exists)
(def E_FILE_FORMAT    ::e-file-formmat)

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

(defn- trim-ext [^String file-name]
  (let [s (trim file-name)]
    (if-let [[_ _ ext] (re-matches #"(?i)(.+)(\.[a-z0-9]{2,4})$" s)]
      (subs s 0 (- (.length s) (.length ext)))
      s)))
;;

(comment

  (re-matches #"^(.+)(\.gpx)$" "123.gpx")
  
  (trim-ext "123.gpx")
  ,)

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
    (str-join "/" (concat [prefix] yymmdd [file-name]))))
;;

(defn save-gpx-file [message public?]
  (let [{ document  :document
          caption   :caption        
          from      :from}          message
        ;
        { file-id   :file_id 
          file-size :file_size 
          file-name :file_name}     document

        { user-id   :id}            from]
          ; username    :username
          ; frist-name  :first_name
          ; last-name   :last_name}   from]
    ;;
    (if (> file-size FILE_SIZE_MAX)
      (do
        (warn "file too big:" file-size)
        { :error      E_FILE_TOO_BIG
          :file-size  file-size})
      ;;  
      (let [{body :body}  (file-fetch           file-id cfg/tg)
            hash          (byte-array-hash-str  HASH_FN body)
            h-trk         (track-by-hash        hash)]
        (if (and public? h-trk)
          (do
            (debug "already hashed:" {:user-id user-id :track-id (:id h-trk)})
            { :error      E_FILE_EXISTS
              :old-track  h-trk})
          ;;
          (if-let [_gpx (parse-bytes body)]
            (let [id    (next-track-id)
                  path  (write-data id body)
                  title (if (not-blank? caption) caption (trim-ext file-name))]

              ; - geom    {box? center? bounds?}
              { :track
                (create-track id
                  { :type     TRACK_TYPE_GPX
                    :user_id  (tg->user-id user-id)
                    :status   (if public? TRACK_STATUS_PUBLIC TRACK_STATUS_PRIVATE)
                    :hash     hash
                    :file     {:path path :size file-size}
                    :orig     {:telegram message}
                    :title    (subs title 0 TRACK_TITLE_LENGTH)})})
                    ;; descr  TRACK_DESCR_LENGTH
                    ;; geom
            ;;
            (do
              (warn "incorrect gpx format:" document)
              { :error  E_FILE_FORMAT})))))))
;;

;;.
