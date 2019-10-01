
(ns gtb.app.inbound
  (:require
    [mlib.logger :refer [debug warn]]))
;=


(defn h-message [message]
  (debug "message:" message))
;;

(defn handle-update [update]

  ; (debug "handle-update:" update)

  (condp #(get %2 %1) update
    :message :>> h-message
    false))
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
