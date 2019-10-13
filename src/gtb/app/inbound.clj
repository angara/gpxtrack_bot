
(ns gtb.app.inbound
  (:require
    [clojure.string :refer  [lower-case ends-with?]]
    ;
    [mlib.logger    :refer  [debug warn]]
    [mlib.telegram  :refer  [api]]
    [mlib.util      :refer  [to-int]]
    ;
    [gtb.const      :refer  [MIME_GPX]]
    [gtb.app.cfg    :as     cfg]
    [gtb.app.hgroup :refer  [group-message]]
    [gtb.app.hpriv  :refer  [priv-message]]
    [gtb.app.search :refer  [search-title]]))
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


(defn- answer-results [tracks]
  (for [trk tracks]
    {
      :type   "article"
      :id     (:id trk)
      :title  (str "id:" (:id trk) " - "
                  (:title trk (-> trk :info :title)))
      ; :description            (:descr trk)
      :input_message_content  { :message_text (str "track:" (:id trk))
                                :parse_mode "HTML"}}))
      ; reply_markup
      ; url
;;

(defn h-inline [{:keys [id query offset]}]    ;; from
  (debug "query:" query)
  (when (<= 3 (count query))
    (let [PAGE      10
          ofs       (to-int offset 0)
          tracks    (search-title query ofs PAGE)
          next-ofs  (if (< (count tracks) PAGE) "" (str (+ ofs PAGE)))
          results   (answer-results tracks)]
      ;
      (api "answerInlineQuery"
        { :inline_query_id  id
          :next_offset      next-ofs
          :results          results}
        cfg/tg))))
;;

(defn handle-update [update]
  (try
    (condp #(get %2 %1) update
      :message      :>> h-message
      :inline_query :>> h-inline
      false)
    (catch Exception ex
      (let [edata (or (ex-data ex) {:message (.getMessage ex)})]
        (warn "handle-update.catch:" edata ex)))))
  ;
;;

;;.
 
