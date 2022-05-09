
#_(ns Leito.Belle.dateseeds
    (:require [clojure.java.io :as io]
              [clojure.pprint :as pp]
              [clojure.string :as str]
              [clojure.reflect :as r]
              [clj-http.client :as client]
              [shadow.undertow.impl :refer [RespondBody]]
              [clojure.java.io :as io]
              #_[srv.server])
    (:import (io.undertow.util HeaderMap HeaderValues Headers HttpString)
             (io.undertow.server HttpServerExchange)
             (java.nio ByteBuffer)
             (io.undertow.io Sender)
             (java.io InputStream File)
             (io.undertow.server.handlers ResponseCodeHandler)))

(defn deep-merge [a & maps]
  (if (map? a)
    (apply merge-with deep-merge a maps)
    (apply merge-with deep-merge maps)))

(def not-found
  {:status 404
   :headers {"content-type" "text/plain"}
   :body "Not found."})

(def settings (atom {:proxy-geoserver-host "http://geoserver:8080/geoserver"
                     :proxy-path "/geoserver"}))

(defn update-settings!
  [m]
  (prn "updating settings")
  (prn m)
  (reset! settings
          (merge @settings m)))



(defn get-proxy-path
  []
  (or (:proxy-path @settings) ""))

(defn get-geoserver-host
  []
  (or (:proxy-geoserver-host @settings) ""))

; https://stackoverflow.com/questions/13924842/extend-clojure-protocol-to-a-primitive-array
; https://github.com/thheller/shadow-cljs/blob/61af1cce91398c77f941a3b057cbb840b384eaf6/src/main/shadow/undertow/impl.clj

(def ut-body (atom nil))
(def ut-exchange (atom nil))

#_(do (pp/pprint @ut-body))
#_(do (pp/pprint @ut-exchange))


(extend-protocol RespondBody
  #_(Class/forName "[B") #_(class (float-array 0))

  (Class/forName "[B")
  (respond [body ^HttpServerExchange exchange]
    #_(do
      (prn "protocol RespondBody reposponding to [B")
      (reset! ut-body body)
      (reset! ut-exchange exchange)
      (.send ^Sender (.getResponseSender exchange) body)
      )
    (do 
      (with-open [^InputStream b (io/input-stream body)]
        (io/copy b (.getOutputStream exchange)))
      )
    )

  ; String
  ; (respond [body ^HttpServerExchange exchange]
  ;   (.send ^Sender (.getResponseSender exchange) body))

  ; InputStream
  ; (respond [body ^HttpServerExchange exchange]
  ;   (with-open [^InputStream b body]
  ;     (io/copy b (.getOutputStream exchange))))

  ; File
  ; (respond [f exchange]
  ;   (respond (io/input-stream f) exchange))
  ; 
  )


(def rqs (atom nil))
(def rqs-opts (atom nil))
(def ex (atom nil))
(def rawrsp (atom nil))


(defn prn-methods
  [inst]
  (pp/print-table
   (sort-by :name
            (filter :exception-types (:members (r/reflect inst))))))

#_(do (pp/pprint @rqs))

#_(slurp (:body @rqs))

#_(prn-methods (:body @rqs))

#_(prn-methods (:shadow.undertow.impl/exchange @rqs))

#_(with-open [^InputStream b (:body @rqs)]
    (slurp b))

#_(do (pp/pprint @rqs-opts))

#_(do (pp/pprint @ex))

#_(do (pp/pprint @rawrsp))

#_(do (count (:body @rawrsp)))



#_(prn "---dev-http")

#_(not-empty "")

#_(srv.server/run-dev)

#_(->
   (client/request
    (merge {:method :get
            :url (str GEOSERVER_HOST "/rest/layers.json")}
           {:basic-auth ["admin" "myawesomegeoserver"]}))
   (pp/pprint)
   )

#_ (subs "/geoserver/rest/layers.json" (count "/geoserver"))

#_(-> (get-geoserver-host) (str/split  #"://") (second) (str/split  #"/") (first) ) 

(defn geoserver-host->host
  []
  (-> (get-geoserver-host) 
      (str/split  #"://") 
      (second) 
      (str/split  #"/") 
      (first)
      (str/replace #"geoserver" "localhost")
      ))

(defn handle [{:keys [uri http-roots http-config request-method
                      server-name server-port body headers query-string] :as req}]
  (reset! rqs req)
  #_(println "uri is" uri))
  (cond
    (= uri "/update-settings")
    (let [bstring (with-open [^InputStream b (:body @rqs)]
                    (slurp b))
          r (try (update-settings! (read-string bstring))
                 (catch Exception e (.getMessage e)))]
      {:status 200
       :headers {"content-type" "text/html; charset=utf-8"}
       :body (str @settings)})

    (= uri "/hello")
    {:status 200
     :headers {"content-type" "text/html; charset=utf-8"}
     :body "world!!"}

    (str/starts-with? uri "/geoserver")
    (let [ path (subs uri (count (get-proxy-path)))
          ;path uri
          url (if (not-empty query-string)   (str path "?" query-string) path)
          req-opts (dissoc
                    {:throw-entire-message? true
                     :throw-exceptions false
                     :method request-method
                     :url (str (get-geoserver-host) url)
                     :as (if (str/includes? url "/wms") :byte-array :string)
                     :body body
                     :headers  (->
                                (dissoc headers "content-length"
                                        "host"
                                        "origin"
                                        ; "referer"
                                        "sec-fetch-mode")
                                #_(merge {"host" (geoserver-host->host)}))
                    ; :basic-auth ["admin" "myawesomegeoserver"]
                     }
                    )
          rawres (try (client/request     req-opts)
                      (catch Exception e
                        (do
                          (reset! ex e)
                          {:status 600
                           :headers {"content-type" "text/html; charset=utf-8"}
                           :body (str (.getMessage e))})))
          hdrs (:headers rawres)]
      (reset! rawrsp rawres)
      (reset! rqs-opts req-opts)
      (deep-merge
       rawres
       {;:body (.getBytes (:body rawres))
        ; :body "hello"
        :headers {"Access-Control-Allow-Origin" "*"
                  "Access-Control-Allow-Headers" "*" #_"content-type"}}))

    :else
    (let [accept (get-in req [:headers "accept"])]
      (if (and accept (not (str/includes? accept "text/html")))
        not-found
        (let [index-name
              (get http-config :push-state/index "index.html")

              hdrs
              (get http-config :push-state/headers {"content-type" "text/html; charset=utf-8"})

              index-file
              (reduce
               (fn [_ http-root]
                 (let [file (io/file http-root index-name)]
                   (when (and file (.exists file))
                     (reduced file))))
               nil
               http-roots)]

          (if-not index-file
          ;; FIXME: serve some kind of default page instead
            (assoc not-found :body "Not found. Missing index.html.")
            {:status 200
             :headers hdrs
             :body (slurp index-file)})))))
  
  