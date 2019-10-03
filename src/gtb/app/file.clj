
(ns gtb.app.file
  (:require
    [mlib.crypto    :refer [byte-array-hash-str]]
    [mlib.logger    :refer [debug warn]]
    [mlib.telegram  :refer [file-fetch] :as tg]
    ;
    [gtb.app.core   :refer [tg-cfg]]
    [gtb.db.core    :refer [inc-var TRACK_SEQ_VAR]]
    [gtb.gpx.core   :refer [parse-bytes]]))
;=

(def HASH_FN "SHA-1")

(defn next-track-id []
  (str (inc-var TRACK_SEQ_VAR)))
;;



(defn save-gpx-file [message]
  (debug "save-chat-file:" (:document message))

  ;; file_name file_size
  (let [{{file-id :file_id} :document} message
        ;
        {body :body :as fr} (file-fetch file-id tg-cfg)
        hash (byte-array-hash-str HASH_FN body)
        gpx  (parse-bytes body)]
    ;
    (debug "file:" hash fr)))

  
  ;; parse gpx
  
  ;; save file (seq++)
;;

;;,
