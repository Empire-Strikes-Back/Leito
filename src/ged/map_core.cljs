(ns ged.map-core
  (:require  [reagent.core :as r]
             [cljs.repl :as repl]
             [cljs.pprint :as pp]
             [re-frame.core :as rf]
             [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
             [ged.map-ol :as ol]
             ["ol/format/filter" :as olf]
             ["ol/source/Vector" :default OlVectorSource]
             ["ol/layer/Vector" :default OlVectorLayer]
             ["ol/format/GeoJSON" :default OlFormatGeoJSON]
             ["ol/interaction/Select" :default OlInteractionSelect]
             ["ol/interaction/Modify" :default OlInteractionModify]))

(def ^:export astate
  (atom {:olmap nil
         :mapbox nil
         :modify-session {:select nil
                          :modify nil
                          :draw nil
                          :layer nil
                          :source nil}}))

(defn get-key
  [k]
  (k @astate))

(defn assoc-key!
  [k v]
  (swap! astate assoc k v))

(defn set-olmap!
  [olmap]
  (assoc-key! :olmap olmap))

(defn merge-state!
  [m]
  (swap! astate merge m))

(defn get-olmap
  []
  (get-key :olmap))

(defn set-modify-session!
  [m]
  (assoc-key! :modify-session m))

(defn get-modify-session
  []
  (:modify-session @astate))

(defn get-modify-features
  [opts]
  (->
   (.getFeatures (:source (get-modify-session)))
   (ol/features->geojson opts)
   (aget "features")))


; re-frame events


(rf/reg-event-fx
 ::create-olmap
 (fn-traced [{:keys [db]} [_ ea]]
            (if-not (get-olmap)
              (do
                (->
                 (ol/create-map {:el-id ea})
                 (set-olmap!))
                (set! (.. js/window -map) (get-olmap)))
              (do (ol/set-target (get-olmap) ea)))
            {}))

(rf/reg-event-fx
 ::set-olmap-target
 (fn-traced [{:keys [db]} [_ ea]]
            (do
              (when (get-olmap)
                (ol/set-target (get-olmap) ea)))
            {}))

(rf/reg-event-fx
 ::refetch-wms-layers
 (fn-traced
  [{:keys [db]} [_ ea]]
  (do
    (let [lrs (.getArray  (.getLayers (get-olmap)))]
      (doseq [lr  lrs]
        (when (.get lr "id")
          (.updateParams (.getSource lr) #js {:r (Math/random)})))))
  {}))

(rf/reg-event-fx
 ::refetch-wms-layer
 (fn-traced
  [{:keys [db]} [_ ea]]
  (do
    (let [lr (ol/id->layer (get-olmap) ea)]
      (when lr
        (.updateParams (.getSource lr) #js {:r (Math/random)}))))
  {}))

(rf/reg-event-fx
 ::sync-layer-ids
 (fn-traced
  [{:keys [db]} [_ ea]]
  #_(js/console.log ea)
  (do
    (let [[ids {:keys [geoserver-host wms-use-auth? credentials]}] ea
          lrs (.getArray (.getLayers (get-olmap)))]
      (doseq [lr lrs]
        (let [id (.get lr "id")]
          (when id
            (when-not (some #(= id %) ids)
              (.removeLayer (get-olmap) lr)))))
      (doseq [id ids]
        (when-not (ol/id->layer (get-olmap) id)
          (ol/upsert-wms-layer (get-olmap) {:geoserver-host  geoserver-host
                                            :wms-use-auth? wms-use-auth?
                                            :credentials credentials
                                            :id id})))))
  {}))

(defn id->layer
  [olmap id]
  (->>
   (.getLayers olmap)
   (.getArray)
   (filter (fn [lr] (= (.get lr "id") id)))
   (first)))

(rf/reg-event-fx
 ::mapclick-listen
 (fn-traced
  [{:keys [db]} [_ ea]]
  (do
    (let [{:keys [on-click]} ea]
      (when (get-olmap)
        (.on (get-olmap) "singleclick" on-click))))
  {}))

(rf/reg-event-fx
 ::mapclick-unlisten
 (fn-traced
  [{:keys [db]} [_ ea]]
  (do
    (let [{:keys [on-click]} ea]
      (when (get-olmap)
        (.un (get-olmap) "singleclick" on-click))))
  {}))

(rf/reg-event-fx
 ::wfs-search-mapclick
 (fn-traced
  [{:keys [db]} [_ ea]]
  (do
    (let [[ev] ea
          coords (.. ev -coordinate)
          geom-name (:ged.db.settings/geometry-name db)
          geom (ol/point->cir-poly-geom (get-olmap) coords 16)
          filter (olf/intersects geom-name  geom)
                    ; wkt (ol/point->wkt-cir-poly {:coords coords :radius 8})
          ]
      {:dispatch [:ged.map-evs/wfs-search {:filter filter}]}))))

(rf/reg-event-fx
 ::wfs-modify-mapclick
 (fn-traced
  [{:keys [db]} [_ ea]]
  (do
    (let [[ev] ea
          coords (.. ev -coordinate)
          geom-name (:ged.db.settings/geometry-name db)
          geom (ol/point->cir-poly-geom (get-olmap) coords 16)
          filter (olf/intersects geom-name  geom)
                    ; wkt (ol/point->wkt-cir-poly {:coords coords :radius 8})
          ]
      {:dispatch [:ged.map-evs/modify-wfs-click {:filter filter}]}))))

(rf/reg-event-fx
 ::wfs-search-mapbox-listen
 (fn-traced
  [{:keys [db]} [_ ea]]
  (do
    (let [{:keys [on-draw-end]} ea]
      (->>
       (ol/add-box-interaction (get-olmap) {:on-draw-end on-draw-end})
       (assoc-key! :mapbox))))
  {}))

(rf/reg-event-fx
 ::wfs-search-mapbox-unlisten
 (fn-traced
  [{:keys [db]} [_ ea]]
  (do
    (let []
      (when (get-olmap)
        (ol/remove-interaction (get-olmap) (get-key :mapbox)))))
  {}))

(rf/reg-event-fx
 ::wfs-search-mapbox
 (fn-traced
  [{:keys [db]} [_ ea]]
  (do
    (let [[ev] ea
          geom (.getGeometry (.-feature ev))
          geom-name (:ged.db.settings/geometry-name db)
          filter (olf/intersects geom-name  geom)
          ]
      {:dispatch [:ged.map-evs/wfs-search {:filter filter}]}))))

(rf/reg-event-fx
 ::add-modify-interactions
 (fn-traced
  [{:keys [db]} [_ ea]]
  (when (and (get-olmap) (-> @astate :modify-session :source))
   (do
     (let [source (-> @astate :modify-session :source)
           select (OlInteractionSelect.
                   {"features" (.getFeatures source)})
           modify (OlInteractionModify.
                   #js {"features" (.getFeatures select)})]
       (do
         (.addInteraction (get-olmap) select)
         (.addInteraction (get-olmap) modify)
         (swap! astate update-in [:modify-session] assoc :select select)
         (swap! astate update-in [:modify-session] assoc :modify modify)
         #_(.on modify "modifyend" (fn [ev] (js/console.log ev))))
       )))
  {}))

(rf/reg-event-fx
 ::remove-modify-interactions
 (fn-traced
  [{:keys [db]} [_ ea]]
  (when (get-olmap)
    (do
      (.removeInteraction (get-olmap) (-> @astate :modify-session :select))
      (.removeInteraction (get-olmap) (-> @astate :modify-session :modify))
      (swap! astate update-in [:modify-session] assoc :select nil)
      (swap! astate update-in [:modify-session] assoc :modify nil)))
  {}))

(rf/reg-event-fx
 ::sync-modified-features
 (fn-traced
  [{:keys [db]} [_ ea]]
  (when (get-olmap)
    (do
      (when-not (-> @astate :modify-session :source)
        (let [src (OlVectorSource. #js {"wrapX" false})
              lr (OlVectorLayer.
                  #js {"source" src})]
          (.addLayer (get-olmap) lr)
          (swap! astate update-in [:modify-session] assoc :source src)
          (swap! astate update-in [:modify-session] assoc :layer lr))))
    (let [{:keys [features]} ea
          features-vec (-> features (vals) (vec))
          ids (mapv #(:id %) features-vec)
          source (-> @astate :modify-session :source)
          fts (.getFeatures source)]
      (doseq [ft fts]
        (let [id (aget ft "id_")]
          (when id
            (when-not (some #(= id %) ids)
              (.removeFeature  source ft)))))
      (doseq [ft features-vec]
        (when-not (ol/id->feature source (:id ft))
          (.addFeature source
                       (.readFeature (OlFormatGeoJSON.
                                      #js {"extractGeometryName" true}) (clj->js ft)))))))
  {}))


; re-frame iceptors

(rf/reg-cofx
 ::olmap
 (fn [cofx _]
   (assoc cofx :olmap (do (get-olmap)))))

(rf/reg-cofx
 ::modify-features
 (fn [{:keys [db] :as cofx} [k]]
   (let [geom-name (:ged.db.settings/geometry-name db)]
     (assoc cofx :modify-features (do (get-modify-features {:geometry-name geom-name})))
     )
   ))
