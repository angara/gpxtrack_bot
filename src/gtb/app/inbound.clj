
(ns gtb.app.inbound
  (:require
    [clojure.string :refer [lower-case ends-with?]]
    ;
    [mlib.logger    :refer [debug warn]]
    ;
    [gtb.const      :refer [MIME_GPX]]
    [gtb.app.hgroup :refer [group-message]]
    [gtb.app.hpriv  :refer [priv-message]]))
;=


(def PRIVATE_CHAT "private")


(defn gpx-message? [{d :document}]
  (when-let [f (:file_name d)]
    (or
      (= MIME_GPX (:mime_type d))
      (-> f
        (lower-case)
        (ends-with? ".gpx")))))
;;


(defn h-message [message]
  (if (= PRIVATE_CHAT (get-in message [:chat :type]))
    (priv-message   message (gpx-message? message))
    (group-message  message (gpx-message? message))))
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

;;.
 