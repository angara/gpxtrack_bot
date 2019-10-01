
(ns mlib.telegram
  (:require
    [clojure.string :refer [escape]]
    [clj-http.client :as http]
    [jsonista.core :as json]
    [mlib.logger :refer [info warn]]))
;=


(def RETRY_COUNT      5)
(def SOCKET_TIMEOUT   8000)
; (def SOCKET_ERR_DELAY 1000)

(def LONGPOLL 10)

(def E_RETRY_LIMIT   ::E_RETRY_LIMIT)


(defn api-url [token method]
  (str "https://api.telegram.org/bot" token "/" (name method)))
;

(defn file-url [token path]
  (str "https://api.telegram.org/file/bot" token "/" path))
;

(defn hesc [text]
  (escape text {\& "&amp;" \< "&lt;" \> "&gt;" \" "&quot;"}))
;


(def ^:dynamic *opts*)

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

(defn api-call [token method params & [{:keys [timeout proxy]}]]
  (let [url   (api-url token method)
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

(defn api [token method params & [opts]]
  (let [opts (or opts *opts*)]
    (Thread/sleep 20)
    (loop [retry (or (:retry opts) RETRY_COUNT)]
      (let [res
            (try
              (let [{:keys [status body]} (api-call token method params opts)]
                (if (= 200 status)
                  body
                  (if (-> body (:error_code) (str) (first) #{\3 \5}) ;; 3xx or 5xx codes
                    ::retry
                    {:error (assoc body :status status)})))
              (catch Exception ex
                {:error {:exception ex}}))]
            ;
        (if (and (= ::retry res) (< 0 retry))
          (recur (dec retry))
          res)))))
;;

(defn get-updates [token offset limit & [opts]]
  (let [longpoll (:longpoll opts LONGPOLL)
        timeout  (:timeout  opts SOCKET_TIMEOUT)]
    (api token "getUpdates" 
      {:offset offset :limit limit :timeout longpoll} 
      (assoc opts :timeout (+ timeout (* 1000 longpoll))))))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn send-text [token chat-id text]
  (api token :sendMessage
    {:chat_id chat-id :text text :parse_mode "HTML"}))
;

(defn send-message [token chat-id params]
  (api token :sendMessage (merge {:chat_id chat-id} params)))
;

(defn file-path [token file-id]
  ;; {:file_id "..." :file_size 999 :file_path "dir/file.ext"}
  (:file_path
    (api token :getFile {:file_id file-id})))
;

(defn get-file [token file-id & [{timeout :timeout}]]
  (if-let [path (file-path token file-id)]
    (try
      (:body
        (http/get (file-url token path)
          { :as :byte-array
            :socket-timeout (or timeout SOCKET_TIMEOUT)
            :conn-timeout   (or timeout SOCKET_TIMEOUT)}))
      (catch Exception e
        (warn "get-file:" file-id (.getMessage e))))
    ;
    (info "get-file - not path for file_id:" file-id)))
;

(defn send-file
  "params should be stringable (json/generate-string)
    or File/InputStream/byte-array"
  [token method mpart & [{timeout :timeout}]]
  (try
    (let [tout (or timeout SOCKET_TIMEOUT)
          res (:body
                (http/post (api-url token method)
                  { :multipart
                      (for [[k v] mpart]
                        {:name (name k) :content v :encoding "utf-8"})
                    :as :json
                    :throw-exceptions false
                    :socket-timeout tout
                    :conn-timeout tout}))]
          ;
      (if (:ok res)
        (:result res)
        (info "send-file:" method res)))
    (catch Exception e
      (warn "send-file:" method (.getMessage e)))))
;


(defn set-webhook-cert [token url cert-file]
  (http/post (api-url token :setWebhook)
    {:multipart [ {:name "url" :content url}
                  {:name "certificate" :content cert-file}]}))
;

;;.
