
(ns gtb.app.inbound
  (:require
    [mlib.logger    :refer [debug warn]]
    [mlib.telegram  :refer [send-message]]
    ;
    [gtb.const      :refer [MIME_GPX]]
    [gtb.app.cfg    :as    cfg]
    [gtb.app.priv   :refer [priv-message]]
    [gtb.app.file   :refer [save-gpx-file]]
    [gtb.app.track  :as    trk]))
;=


(def PRIVATE_CHAT "private")


(defn group-message [message]
  (debug "group-message:" message)
  (cond
    ;
    (= MIME_GPX (-> message :document :mime_type))
    (when-let [track (save-gpx-file message)]
      (debug "track:" track)
      (send-message
        (-> message :chat :id)
        { :text (trk/describe track)
          :parse_mode "HTML"}
        cfg/tg))
    ;
    :else 
    false))
;;

(defn h-message [message]
  (if (= PRIVATE_CHAT (get-in message [:chat :type]))
    (priv-message   message)
    (group-message  message)))
;;

(defn handle-update [update]
  (try
    (condp #(get %2 %1) update
      :message :>> h-message
      false)
    (catch Exception ex
      (let [edata (or (ex-data ex) {:message (.getMessage ex)})]
        (warn "handle-update.catch:" edata ex)))))
  ;
;;


(comment

  (condp #(get %2 %1) {:a :b}
    :a :>> prn
    nil))

;;.

  ;; {:update_id 25027593, 
  ;;    :message {
          ; :caption "my track", 
          ; :date 1569926493, 
          ; :chat {
          ;   :first_name "Maxim", :username "mpenzin", :type "private", 
          ;   :id 369028, :last_name "Penzin"}, 
          ; :document {
          ;   :file_name "2638112091.gpx", :mime_type "application/gpx+xml", 
          ;   :file_size 51115, :file_id "BQADAgADdwQAAmodmUggn1iMY0dYtRYE"}, 
          ; :message_id 11, 
          ; :from {
          ;   :first_name "Maxim", :language_code "en", :is_bot false, 
          ;   :username "mpenzin", :id 369028, :last_name "Penzin"}


; { :update_id 25027614, 
;   :message 
;   {
;     :date 1569994458, 
;     :message_id 15
;     :chat 
;     {
;       :username "gpxtrack_chat", 
;       :type "supergroup", 
;       :title "GPX Track chat", 
;       :id -1001296157528}, 
;     :document 
;     {
;       :file_name "2638112091.gpx", 
;       :mime_type "application/gpx+xml", 
;       :file_size 51115, 
;       :file_id "BQADAgADzwQAAgO-qEjFATCx7W7NgRYE"}, 
;     :from {
;       :first_name "maxp.dev", 
;       :language_code "en", 
;       :is_bot false, 
;       :username "maxp_dev", 
;       :id 199284482}}}

