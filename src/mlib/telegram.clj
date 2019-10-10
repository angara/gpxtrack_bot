
(ns mlib.telegram
  (:require
    [clojure.string :refer [escape]]
    [clj-http.client :as http]
    [jsonista.core :as json]))
;=


(def RETRY_COUNT      5)
(def SOCKET_TIMEOUT   8000)
; (def SOCKET_ERR_DELAY 1000)

(def LONGPOLL 10)

(def E_RETRY_LIMIT   ::E_RETRY_LIMIT)


(defn api-url [^String apikey ^String method]
  (str "https://api.telegram.org/bot" apikey "/" method))
;

(defn file-url [^String apikey ^String path]
  (str "https://api.telegram.org/file/bot" apikey "/" path))
;

(defn hesc [text]
  (when (string? text)
    (escape text {\& "&amp;" \< "&lt;" \> "&gt;" \" "&quot;"})))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def json-read-keyword-mapper
  (json/object-mapper {:decode-key-fn true}))

(defn parse-json-string [^String s]
  (json/read-value s json-read-keyword-mapper))
;;

(defn json-body [resp]
  (let [ctype (get-in resp [:headers "content-type"])
        body  (:body resp)]
    (if (and (string? body) (string? ctype) (.startsWith ctype "application/json"))
      (assoc resp :body (parse-json-string body))
      resp)))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn api-call [method params {:keys [apikey timeout proxy]}]
  (let [url   (api-url apikey method)
        body  (json/write-value-as-bytes params)
        data  {:content-type      :json
               :body              body
               :throw-exceptions  false
               :socket-timeout    (or timeout SOCKET_TIMEOUT)
               :conn-timeout      (or timeout SOCKET_TIMEOUT)
               :proxy-host        (:host proxy)
               :proxy-port        (:port proxy)}]
    ;;
    (->
      (http/post url data)
      (json-body))))
;;

(defn api [^String method params opts]
  (Thread/sleep 20)
  (loop [retry (or (:retry opts) RETRY_COUNT)]
    (let [res
          (let [{:keys [status body]} (api-call method params opts)]
            (if (= 200 status)
              (:result body)
              (if (-> body (:error_code) (str) (first) #{\3 \5}) ;; 3xx or 5xx codes
                ::retry
                (throw (ex-info "tgapi-call"
                          (assoc body :status status))))))]
          ;
      (if (and (= ::retry res) (< 0 retry))
        (recur (dec retry))
        res))))
;;

(defn get-updates [offset limit opts]
  (let [longpoll (:longpoll opts LONGPOLL)
        timeout  (:timeout  opts SOCKET_TIMEOUT)]
    (api "getUpdates" 
      {:offset offset :limit limit :timeout longpoll} 
      (assoc opts :timeout (+ timeout (* 1000 longpoll))))))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn send-text [chat-id text opts]
  (api "sendMessage" {:chat_id chat-id :text text :parse_mode "HTML"} opts))
;

(defn send-message [chat-id params opts]
  (api "sendMessage" (assoc params :chat_id chat-id) opts))
;

(defn file-info 
  "{:file_id \"...\" :file_size 999 :file_path \"dir/file.ext\""
  [file-id opts]
  (api "getFile" {:file_id file-id} opts))
;

(defn file-fetch [file-id opts]
  (let [;
        {file-path :file_path :as rc} (file-info file-id opts)
        ;
        url       (file-url (:apikey opts) file-path)
        timeout   (or (:timeout opts) SOCKET_TIMEOUT)
        proxy     (:proxy opts)
        data      { :as               :byte-array
                    :socket-timeout   timeout
                    :conn-timeout     timeout
                    :proxy-host       (:host proxy)
                    :proxy-port       (:port proxy)
                    :throw-exceptions false}
        ;
        {status :status body :body} 
        (http/get url data)]
    ;
    (if (= 200 status)
      (assoc rc :body body)
      (throw (ex-info "http/get failed"
                {:status status :body body})))))
;;

; (defn send-file
;   "params should be stringable (json/generate-string)
;     or File/InputStream/byte-array"
;   [method mpart & [{timeout :timeout :as opts}]]
;   (try
;     (let [tout (or timeout SOCKET_TIMEOUT)
;           res (:body
;                 (http/post (api-url (:apikey opts) method)
;                   { :multipart
;                       (for [[k v] mpart]
;                         {:name (name k) :content v :encoding "utf-8"})
;                     :as :json
;                     :throw-exceptions false
;                     :socket-timeout tout
;                     :conn-timeout tout}))]
;           ;
;       (if (:ok res)
;         (:result res)
;         (info "send-file:" method res)))
;     (catch Exception e
;       (warn "send-file:" method (.getMessage e)))))
; ;


; (defn set-webhook-cert [url cert-file]
;   (http/post (api-url apikey "setWebhook")
;     {:multipart [ {:name "url" :content url}
;                   {:name "certificate" :content cert-file}]}))
; ;

;;.
