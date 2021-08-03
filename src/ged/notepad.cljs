(ns ged.notepad
  (:require [clojure.string :as str]
            [clojure.repl :as repl]
            [ged.core :refer [deep-merge]]
            ["ol/format/filter" :as OlFilter]))

#_(OlFilter/like "name" "Mississippi*")

#_(OlFilter/ilike "name" "Mississippi*")

(defn auth-creds
  []
  (str "Basic " (js/btoa (str "admin" ":" "myawesomegeoserver"))))


(defn fetch-geosrv
  [path opts]
  (->
   (js/fetch (str "/geoserver" path)
             (clj->js (deep-merge
                       {"headers" {"Authorization"  (auth-creds)}}
                       opts)))))

(defn url-search-params
  [prms & {:keys [str] :or {str false}}]
  (let [usp (js/URLSearchParams. (clj->js prms))]
    (if str
      (.toString usp)
      usp)))

(defn fetch-geosrv-edn
  ([path]
   (fetch-geosrv-edn path {}))
  ([path opts]
   (->
    (fetch-geosrv path opts)
    (.then (fn [res] (.json res)))
    (.then (fn [j] (js->clj j  :keywordize-keys true))))))

; curl -u admin:myawesomegeoserver -XGET http://localhost:8801/geoserver/rest/layers.json

; headers.set ('Authorization', 'Basic '+ Buffer.from (username + ":" + password) .toString ('base64'));
;window.btoa (username + ':' + password)

#_"http://localhost:8600/geoserver/rest/layers.json"
#_"http://localhost:8801/geoserver/rest/layers.json"
#_"http://localhost:8080/v1"
#_"http://localhost:8801"
#_"http://localhost:8600/geoserver/web/"
#_"http://localhost:8600/geoserver/web/wicket/bookmarkable/org.geoserver.web.demo.MapPreviewPage?2"
#_(->
   (js/fetch "http://localhost:8801"
             (clj->js {"method" "GET"
                      ; "headers" {"Authorization"
                      ;            (str "Basic " (js/btoa (str "admin" ":" "myawesomegeoserver")))
                      ;            }
                       }))
   (.then (fn [res] (.text res)))
   (.then (fn [r] (js/console.log r))))

#_(->
   (js/fetch "/hello"
             (clj->js {"method" "GET"}))
   (.then (fn [res] (.text res)))
   (.then (fn [r] (js/console.log r))))

#_(->
   (js/fetch "/geoserver/rest/layers.json"
             (clj->js {"method" "GET"}))
   (.then (fn [res] (.text res)))
   (.then (fn [r] (js/console.log r))))

#_(->
   (js/fetch "/geoserver/rest/layers.json"
             (clj->js {"method" "GET"
                       "headers" {"Authorization"
                                  (str "Basic " (js/btoa (str "admin" ":" "myawesomegeoserver")))}}))
   (.then (fn [res] (.json res)))
   (.then (fn [j] (js->clj j  :keywordize-keys true)))
   (.then (fn [r] (js/console.log r))))


#_(->
   (fetch-geosrv-edn "/rest/layers.json")
   (.then (fn [r] (js/console.log r))))

; https://docs.geoserver.org/stable/en/user/services/wfs/reference.html

#_(->
   (js/URLSearchParams. #js {"service" "wfs"
                             "version" "2.0.0"
                             "request" "GetFeature"
                             "count" 10
                             "typeNames" "dev:usa_major_cities"
                             "exceptions" "application/json"
                             "outputFormat" "application/json"})
   (.toString))

#_(url-search-params {"service" "wfs"
                      "version" "2.0.0"
                      "request" "GetFeature"
                      "count" 10
                      "typeNames" "dev:usa_major_cities"
                      "exceptions" "application/json"
                      "outputFormat" "application/json"} :str true)

#_(->
   (fetch-geosrv-edn
    (str "/wfs?" (url-search-params {"service" "wfs"
                                     "version" "2.0.0"
                                    ;  "version" "1.1.0"
                                     "request" "GetFeature"
                                     "count" 10
                                     "typeNames" "dev:usa_major_cities"
                                     "exceptions" "application/json"
                                     "outputFormat" "application/json"} :str true))
    {:method "get"})
   (.then (fn [r] (js/console.log r))))

#_(->
   (fetch-geosrv-edn
    (str "/wfs?" (url-search-params {"service" "wfs"
                                     "version" "1.1.0"
                                     "request" "DescribeFeatureType"
                                     "typeName" "dev:usa_major_cities"
                                    ;  "typeNames" "dev:usa_major_cities"
                                     "exceptions" "application/json"
                                     "outputFormat" "application/json"} :str true))
    {:method "get"})
   (.then (fn [r] (js/console.log r))))

#_(->>
   (wfs-get-features-body {:offset 0
                           :limit 10
                           :featurePrefix "dev"
                           :featureTypes ["usa_major_cities"]})
   (.serializeToString (js/XMLSerializer.)))

#_(->
   (fetch-geosrv-edn
    "/wfs"
    {:method "post"
     :body (->
            (wfs-get-features-body {:offset 0
                                    :limit 10
                                    :featurePrefix "dev"
                                    :featureTypes ["usa_major_cities"]})
            (xml->str))})
   (.then (fn [r] (js/console.log r))))

; using ecql

#_(->
   (fetch-geosrv-edn
    (str "/wfs?" (url-search-params {"service" "wfs"
                                     "version" "2.0.0"
                                    ;  "version" "1.1.0"
                                     "request" "GetFeature"
                                     "count" 10
                                     "typeNames" "dev:usa_major_cities"
                                     "exceptions" "application/json"
                                     "cql_filter" "NAME ilike \n '%hono%' "
                                     "outputFormat" "application/json"} :str true))
    {:method "get"})
   (.then (fn [r] (js/console.log r))))