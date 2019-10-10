
(ns gtb.app.file
  (:require
    [clojure.java.io  :as     io]
    [clojure.string   :as     str]
    [java-time        :as     jt]
    [mlib.crypto      :refer  [byte-array-hash-str]]
    [mlib.logger      :refer  [debug warn]]
    [mlib.telegram    :refer  [file-fetch] :as tg]
    ;
    [gtb.const        :refer 
      [ TRACK_STATUS_PUBLIC 
        TRACK_STATUS_PRIVATE 
        TRACK_TYPE_GPX]]
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

(defn save-gpx-file [message public?]
  ; (debug "save-chat-file:" (:document message))

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
        E_FILE_TOO_BIG)
      ;;
      (let [{body :body}  (file-fetch           file-id cfg/tg)
            hash          (byte-array-hash-str  HASH_FN body)
            h-trk         (track-by-hash        hash)]
        (if (and public? h-trk)
          (do
            (debug "already hashed:" {:user-id user-id :track-id (:id h-trk)})
            E_FILE_EXISTS)
          ;;
          (if-let [_gpx (parse-bytes body)]
            (let [id    (next-track-id)
                  path  (write-data id body)
                  title (if (not-blank? caption) caption file-name)]

              ; - info    {:title "source/telegram/caption" :tags [...] :related [...], :num_seg 999}
              ; - geom    {box? center? bounds?}

              (create-track id
                { :type     TRACK_TYPE_GPX
                  :user_id  (tg->user-id user-id)
                  :status   (if public? TRACK_STATUS_PUBLIC TRACK_STATUS_PRIVATE)
                  :hash     hash
                  :file     {:path path :size file-size}
                  :orig     {:telegram message}
                  :info     {:title title}}))
                  ;; geom
            ;;
            (do
              (warn "incorrect gpx format:" document)
              E_FILE_FORMAT)))))))
;;

;;.
