(ns Leito.map-ol
  (:require
   [Leito.core :refer [deep-merge]]
   ["ol/proj/Projection" :default OlProjection]
   ["ol/proj" :refer [fromLonLat]]
   ["ol/Map" :default OlMap]
   ["ol/View" :default OlView]
   ["ol/source/OSM" :default OlOSM]
   ["ol/layer/Tile" :default OlTileLayer]
   ["ol/layer/Vector" :default OlVectorLayer]
   ["ol/source/TileWMS" :default TileWMS]
   ["ol/geom/Circle" :default OlGeomCircle]
   ["ol/Feature" :default OlFeature]
   ["ol/format/GeoJSON" :default OlFormatGeoJSON]
   ["ol/format/WKT" :default OlFormatWKT]
   ["ol/geom/Polygon" :as OlGeomPolygon]
   ["ol/interaction/Draw" :default OlDrawInteraction :as OlDraw]
   ["ol/source/Vector" :default OlVectorSource]
   ["ol/layer/Vector" :default OlVectorLayer]
   ["ol/interaction/Select" :default OlInteractionSelect]
   ["ol/interaction/Modify" :default OlInteractionModify]
   ["ol/TileState" :default OlTileState]
   
   
   )
  )

#_OlDraw/createBox

(defn tile-layer-osm
  []
  (OlTileLayer.
   #js {"source" (OlOSM.)}))

(defn create-view
  [opts]
  (OlView.
   (clj->js
    (merge {:projection "EPSG:3857"
            :maxZoom 28

            :center [0 0]
            :zoom 0
            ; :center [-11000000, 4600000]
            ; :zoom 4
            }
           opts))))

(defn create-map
  [{:keys [el-id target]
    :or {el-id "map-container"}}]
  (OlMap.
   (clj->js {:layers [(tile-layer-osm)]
             :target (or target (js/document.getElementById el-id))
             :view (create-view {})
             :pixelRatio 1})))

(defn to-data-url
  [url]
  (->
   (js/fetch url
             (clj->js {;"headers" {"Authorization"  (auth-creds)}
                       :method "get"}))
   (.then (fn [res] (.blob res)))
   (.then (fn [blob]
            (js/Promise.
             (fn [resolve reject]
               (let [r (js/FileReader.)]
                 (do
                   (set! (.. r -onloadend) #(resolve (. r -result)))
                   (set! (.. r -onerror) reject)
                   (.readAsDataURL r blob)))))))))
  
(defn basic-creds
  [uname pass]
  (str "Basic " (js/btoa (str uname ":" pass))))

(defn url->params-string
  [url]
  (->
   (.-search (js/URL. url))
   (js/URLSearchParams.)
   (.toString)))

#_(defn tileLoadFunction
  [tile src]
  (->
   (js/fetch src
         (clj->js
          {"method" "get"
           "referer" "no-referer"
           "headers" {"Authorization" (basic-creds "admin" "hello")}})
             )
   (.then (fn [r] (.blob r)))
   (.then (fn [r]
            (let [durl (js/URL.createObjectURL  r)]
              (do (aset (.getImage tile) "src" durl)))))))

(defn create-tile-load-fn
  [{:keys [wms-use-auth?
           credentials]}]
  (fn [tile src]
    (let [xhr (js/XMLHttpRequest.)]
      (aset xhr "responseType" "blob")
      (.addEventListener xhr "loadend"
                         (fn [ev]
                           (let [data (.-response xhr)]
                             (if data
                               (do (aset (.getImage tile) "src"
                                         (js/URL.createObjectURL  data)))
                               (do (.setState tile (.-ERROR OlTileState)))))))
      (.addEventListener xhr "error"
                         (fn [ev]
                           (do (.setState tile (.-ERROR OlTileState)))))
      (.open xhr "GET" src)
      (when wms-use-auth?
        (.setRequestHeader xhr
                           "Authorization"
                           (basic-creds (:username credentials) (:password credentials)))
        )
      (.send  xhr))))


(defn wms-source
  [{:keys [geoserver-host wms-use-auth? credentials]} src-opts]
  (TileWMS.
   (clj->js
    (deep-merge
     {:url
     #_"http://localhost:8801/geoserver/wms" ; works... unclear how
      #_(str geoserver-host "/wms")
      "/geoserver/wms"
      #_"/geoserver/wms"
      :tileLoadFunction (create-tile-load-fn {:wms-use-auth? wms-use-auth?
                                              :credentials credentials})
      :params {;"LAYERS" "dev:usa_major_cities"
               "TILED" true
               "SRS" "EPSG:3857"
              ;  "exceptions" "application/vnd.ogc.se_inimage"
               "exceptions" "application/json"
               "TRANSPARENT" true
               "FORMAT" "image/png"
               "REQUEST" "GetMap"
               "VERSION" "1.1.1"
               "SERVICE" "WMS"}
      :serverType "geoserver"}
     src-opts))))

(defn wms-layer
  [{:keys [geoserver-host id wms-use-auth? credentials] } lr-opts src-opts]
  (let [lr (OlTileLayer.
            (clj->js
             (deep-merge
              {:source (wms-source
                        {:geoserver-host geoserver-host
                         :wms-use-auth? wms-use-auth?
                         :credentials credentials}
                        (deep-merge {:params  {"LAYERS" id}}  src-opts))}
              (merge
               lr-opts
               {"id" id})
              )))]
    #_(do (.set lr "id" (:id opts)))
    lr))



#_(js/console.log (clj->js {:view (OlView.
                                   {:center [0 0]
                                    :maxZoom 28
                                    :zoom 0})} ))
#_(js/console.log OlView)

#_(js/console.log (OlView.
                   {:center [0 0]
                    :maxZoom 28
                    :zoom 0}))

(defn set-target
  [olmap el-id]
  (.setTarget olmap (js/document.getElementById el-id)))


(defn id->layer
  [olmap id]
  (->>
   (.getLayers olmap)
   (.getArray)
   (filter (fn [lr] (= (.get lr "id") id)))
   (first)))

(defn id->feature
  [source id]
  (->>
   (.getFeatures source)
   (filter (fn [ft] (= (.get ft "id") id)))
   (first)))

(defn refetch-wms-layer
  [olmap id]
  (js/console.log "update wms layer:" id)
  (->
   (id->layer olmap id)
   (.getSource)
   (.updateParams #js {})))


(defn upsert-wms-layer
  [olmap {:keys [geoserver-host id wms-use-auth? credentials]}]
  (let [lr (id->layer olmap id)]
    (when-not lr
      (.addLayer olmap (wms-layer
                        {:geoserver-host geoserver-host
                         :wms-use-auth? wms-use-auth?
                         :credentials credentials
                         :id id} {} {})))))


(defn remove-layer
  [olmap id]
  (let [lr (id->layer olmap id)]
    (.removeLayer olmap lr)))


#_(some #(= "1" %) ["1" "2"])

#_(when-not true (prn 3))


(defn px->meters
  [olmap px]
  (let [resolution (.getResolution (.getView olmap))]
    (->
     (* px resolution)
     (/  2))))

#_(defn point->cir-poly-geom
  [olmap coords radius-px]
  (OlGeomPolygon/circular coords (px->meters olmap radius-px) 16 #_6371008.8))

(defn point->cir-poly-geom
  [olmap coords radius-px]
  (OlGeomPolygon/fromCircle
   (OlGeomCircle. (clj->js coords) (px->meters olmap radius-px))
   16))

(defn point-coords->circlular-polygon
  [{:keys [olmap coords radius]}]
  (OlFeature.
   (clj->js
    {"geometry" (point->cir-poly-geom olmap coords  radius)})))

(defn feature->geojson
  [ft]
  
  (->
   (OlFormatGeoJSON. #js {})
   (.writeFeature ft)))

(defn features->geojson
  [fts {:keys [geometry-name]}]
  (let [json (->
            (OlFormatGeoJSON.
             #js {"extractGeometryName" true
                  "geometryName" true})
            (.writeFeaturesObject fts))
        edn (js->clj json :keywordize-keys true)
        fts (mapv
             (fn [ft] (assoc ft :geometry_name geometry-name))
             (:features edn))]
    (clj->js (merge
              edn
              {:features fts}))))

(defn point-coords->circle-geojson
  [opts]
  (->
   (point-coords->circlular-polygon opts)
   (feature->geojson)))

(defn feature->wkt
  [ft]
  (->
   (OlFormatWKT.)
   (.writeFeature ft)))

(defn point->wkt-cir-poly
  [opts]
  #_(js/console.log opts)
  (->>
   (point-coords->circlular-polygon opts)
   (feature->wkt)))

#_(OlGeomPolygon/circular [0 0 ] 8 16 )


(defn add-box-interaction
  [olmap opts]
  (let [{:keys [on-draw-end]} opts
        source (OlVectorSource. #js {"wrapX" false})
        layer (OlVectorLayer.
               #js {"source" source})
        draw (OlDrawInteraction.
              #js {"type" "Circle"
                   "source" source
                   "geometryFunction" (OlDraw/createBox)})]
    (do
      (.addLayer olmap layer)
      (.on draw "drawend" on-draw-end )
      (.addInteraction olmap draw))
    {:source source
     :layer layer
     :interaction draw}))

(defn remove-interaction
  [olmap opts]
  (let [{:keys [interaction source layer]} opts]
    (do
      (.removeInteraction olmap interaction)
      (.removeLayer olmap layer))))

(defn add-modify-session
  [olmap ftedn]
 
  (let [json (clj->js ftedn)
        ft (.readFeature (OlFormatGeoJSON.
                          #js {"extractGeometryName" true}) json)
        source (OlVectorSource. #js {"wrapX" false
                                     "features" #js [ft]})
        layer (OlVectorLayer.
               #js {"source" source})
        select (OlInteractionSelect.
                {"features" (.getFeatures source)})
        modify (OlInteractionModify.
                #js {"features" (.getFeatures select)})]
    (do
      (.addLayer olmap layer)
      (.addInteraction olmap select)
      (.addInteraction olmap modify)
      #_(.on modify "modifyend" (fn [ev] (js/console.log ev))))
    {:select select
     :modify modify
     :layer layer
     :source source}))

(defn remove-modify-session
  [olmap opts]
  (let [{:keys [select modify
                layer source]} opts]
    (do
      (.removeLayer olmap layer)
      (.removeInteraction olmap select)
      (.removeInteraction olmap modify))))

(defn get-modify-features
  [modify]
  (->
   modify
   (.getOverlay)
   (.getSource)
   (.getFeatures)))