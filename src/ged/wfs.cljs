(ns ged.wfs
  (:require [clojure.repl :as repl]
            [clojure.string :as str]
            ["ol/format/WFS" :default OlFormatWFS]
            ["ol/format/filter" :as olf]
            ["ol/format/GeoJSON" :default OlFormatGeoJSON]))

(defn xml->str
  [x]
  (.serializeToString (js/XMLSerializer.) x))

; https://openlayers.org/en/v6.0.0/examples/vector-wfs-getfeature.html

(defn wfs-get-features-body
  [{:keys [offset limit
           featurePrefix featureTypes
           filter]}]
  (.writeGetFeature (OlFormatWFS.)
                    (clj->js
                     (merge
                      {"srsName" "EPSG:3857"
                       "featurePrefix" featurePrefix
                       "featureTypes" featureTypes
                       "outputFormat" "application/json"
                       "startIndex" offset
                       "maxFeatures" limit
                       "count" limit}
                      (when filter {"filter" filter})))))

(defn jsons->features
  [jsons]
  (when jsons
    (->
     (OlFormatGeoJSON.
      #js {;"geometryName" "the_geom"
           "extractGeometryName" true})
     (.readFeatures
      #js {"type" "FeatureCollection"
           "features" (clj->js jsons)}))))

(defn wfs-transaction-body
  [{:keys [featurePrefix featureType
           featureNS
           srsName
           inserts updates deletes]
    ; :or {inserts #js []
    ;      updates #js []
    ;      deletes #js []}
    }]
  (.writeTransaction (OlFormatWFS.)
                     inserts  updates deletes
                     (clj->js
                      {"srsName" (or srsName "EPSG:3857")
                       "featureNS" featureNS
                       "featurePrefix" featurePrefix
                       "featureType" featureType
                       "version" "1.1.0"
                      ;  "gmlOptions" { 
                      ;                "featureNS" "http://www.opengis.net/wfs/dev"
                      ;                "featureType" (str featurePrefix ":" featureType )
                      ;                "srsName" (or srsName "EPSG:3857")

                      ;                }
                       })))

(defn wfs-tx-jsons
  [opts]
  (let []
    (wfs-transaction-body
     (merge
      opts
      {:inserts (jsons->features (:inserts opts))
       :updates (jsons->features (:updates opts))
       :deletes (jsons->features (:deletes opts))}))))


(defn wfs-tx-jsons-str
  [opts]
  (->
   (wfs-tx-jsons  opts)
   (xml->str)
   #_(str/replace "<Name>geometry</Name>" "<Name>the_geom</Name>")))

(defn wfs-get-features-body-str
  [opts]
  (->
   (wfs-get-features-body opts)
   (xml->str)))

#_(js/eval "3")

#_(let [s "hono"
        create-filter (fn []
                        (js/eval
                         (str "like('NAME', '" "*" s "*" "','*','!', false)"))
                        )]
    (.call create-filter #js {"like" olf/like} )
    )

(defn attrs->eqcl-ilike
  [{:keys [attrs input joiner]}]
  (let []
    (->>
     (mapv
      (fn [attr]
        (str (:name attr) " ilike " "'%" input "%'"))
      attrs)
     (str/join (str " " joiner " ")))))

#_(attrs->eqcl-ilike {:attrs [{:name "x"} {:name "y"}]
                      :joiner "OR"
                      :input "hello"} )