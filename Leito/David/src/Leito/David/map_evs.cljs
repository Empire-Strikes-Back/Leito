(ns Leito.David.map-evs
  (:require [re-frame.core :as rf]
            [clojure.string :as str]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
            [ajax.core :as ajax]
            [Leito.David.wfs :refer [wfs-tx-jsons-str wfs-get-features-body-str]]
            [cognitect.transit :as t]))

(rf/reg-event-fx
 ::tab-button
 (fn-traced [{:keys [db]} [_ ea]]
            (let [kw   (keyword ea)
                  key :Leito.David.db.map/tab-button
                  v-old (key db)
                  v (if (= kw v-old) nil kw)
                  nxdb (assoc db key v)]
              {:db nxdb
               :dispatch [:assoc-in-store [[key] v]]})))

(rf/reg-event-fx
 ::fetch-all-layers
 (fn-traced
  [{:keys [db]} [_ ea]]
  (let []
    {:dispatch
     [:Leito.David.evs/request-2
      {:method :get
       :params {}
       :headers {"Content-Type" "application/json"}
       :path "/geoserver/rest/layers.json"
       :response-format
       (ajax/raw-response-format)
       :expected-success-fmt :json->edn
       :expected-failure-fmt :raw
       :expected-body-fmt :raw
       :on-success [::fetch-all-layers-res]
       :on-failure [::fetch-all-layers-res]}]
     :db db})))

(rf/reg-event-fx
 ::fetch-all-layers-res
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (assoc db :Leito.David.db.map/fetch-all-layers-res ea)}))

(rf/reg-event-fx
 ::all-layers-search-input
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (->
                  db
                  (update-in [:Leito.David.db.map/all-layers-table-mdata :pagination] assoc :current 1)
                  (assoc :Leito.David.db.map/all-layers-search-input ea)) 
             }))

(rf/reg-event-fx
 ::selected-layers-checked
 (fn-traced [{:keys [db]} [_ ea]]
            (let [v (js->clj ea)
                  key :Leito.David.db.map/selected-layers-checked
                  nx (assoc db key v)]
              {:db nx
               :dispatch [:assoc-in-store [[key] (key nx)]]})))

(rf/reg-event-fx
 ::selected-layers-checked-remove-ids
 (fn-traced [{:keys [db]} [_ ea]]
            (let [ids ea
                  k :Leito.David.db.map/selected-layers-checked
                  v (k db)
                  nv (filterv (fn [id] (not (some #(= % id) ids)))  v)
                  nx (assoc db k nv)]
              {:db nx
               :dispatch [:assoc-in-store [[k] nv]]})))

(rf/reg-event-fx
 ::all-layers-checked
 (fn-traced [{:keys [db]} [_ ea]]
            (let [v (js->clj ea)]
              {:db (assoc db :Leito.David.db.map/all-layers-checked v)})
            ))

(rf/reg-event-fx
 ::add-selected-layers-ids
 (fn-traced [{:keys [db]} [_ ea]]
            (let [key :Leito.David.db.map/selected-layers-ids
                  v-old (key db)
                  nx (assoc db key
                            (->> (concat v-old ea)
                                 (distinct)
                                 (vec)))]
              {:db nx
               :dispatch [:assoc-in-store [[key] (key nx)]]})))


(rf/reg-event-fx
 ::remove-selected-layers-id
 (fn-traced [{:keys [db]} [_ ea]]
            (let [k :Leito.David.db.map/selected-layers-ids
                  v-old (k db)
                  id ea
                  nx (assoc db k
                            (filterv #(not= % id) v-old))]
              {:db nx
               :dispatch-n (list
                            [:assoc-in-store [[k] (k nx)]]
                            [::selected-layers-checked-remove-ids [id]])})))

(rf/reg-event-fx
 ::wfs-search-layer-input
 (fn-traced [{:keys [db]} [_ ea]]
            (let [v ea
                  k :Leito.David.db.map/wfs-search-layer-input]
              {:db (assoc db k v)
               :dispatch [:assoc-in-store [[k] v]]})))

(rf/reg-event-fx
 ::wfs-search-area-type
 (fn-traced [{:keys [db]} [_ ea]]
            (let [kw (keyword ea)
                  k :Leito.David.db.map/wfs-search-area-type
                  v-old (k db)
                  v (if (= kw v-old) nil kw)
                  nxdb (assoc db k v)]
              {:db nxdb
               :dispatch [:assoc-in-store [[k] v]]})))

(rf/reg-event-fx
 ::wfs-search
 (fn-traced [{:keys [db]} [_ ea]]
            (let [{:keys [filter]} ea
                  ftype-input (:Leito.David.db.map/wfs-search-layer-input db)
                  last-filter (:Leito.David.db.map/wfs-search-last-filter db)
                  wfs-filter (or filter last-filter)
                  [fpref ftype] (try (str/split ftype-input \:)
                                     (catch js/Error e
                                       (do (js/console.warn e)
                                           ["undefined:undefined"])))
                  table-mdata (:Leito.David.db.map/wfs-search-table-mdata db)
                  total (get-in db [:Leito.David.db.map/wfs-search-res :total])
                  pag (:pagination table-mdata)
                  {:keys [current pageSize]} pag
                  limit (or pageSize 10)
                  offset (or (* pageSize (dec current)) 0)
                  body (wfs-get-features-body-str
                        (merge
                         {:offset offset
                          :limit limit
                          :featurePrefix fpref
                          :featureTypes [ftype]}
                         (when wfs-filter
                           {:filter wfs-filter})))]
              #_(do (editor-request-set! (prettify-xml body)))
              {:dispatch
               #_[:Leito.David.req/request {:profiles [:wfs-get-feature]
                       :params {}
                       :body body
                       :headers {"Content-Type" "application/json"}
                       :on-success [::wfs-search-res]
                       :on-failure [::wfs-search-res]}]
               [:Leito.David.evs/request-2
                  {:method :post
                   :params {}
                   :body body
                   :headers {"Content-Type" "application/json"}
                   :path "/geoserver/wfs?exceptions=application/json"
                   #_(ajax/transit-response-format {:reader (t/reader :json)})
                   :response-format
                   (ajax/raw-response-format)
                   :expected-success-fmt :json->edn
                   :expected-failure-fmt :raw
                   :expected-body-fmt :xml
                   :on-success [::wfs-search-res]
                   :on-failure [::wfs-search-res]}]
               :db (merge db {:Leito.David.db.map/wfs-search-last-filter wfs-filter
                              :Leito.David.db.map/search-table-mdata
                              (merge table-mdata {:pagination (merge pag {:current 1})})})})))

(rf/reg-event-db
 ::wfs-search-res
 (fn-traced [db [_ ea]]
            (assoc db :Leito.David.db.map/wfs-search-res ea)))

(rf/reg-event-fx
 ::wfs-search-table-mdata
 (fn-traced [{:keys [db]} [_ ea]]
            (let [key :Leito.David.db.map/wfs-search-table-mdata]
              {:dispatch [:Leito.David.map-evs/wfs-search {}]
               :db (assoc db key ea)})))

(rf/reg-event-fx
 ::all-layers-table-mdata
 (fn-traced [{:keys [db]} [_ ea]]
            (let [key :Leito.David.db.map/all-layers-table-mdata]
              {:db (assoc db key ea)})))

(rf/reg-event-fx
 ::modify-layer
 (fn-traced [{:keys [db]} [_ ea]]
            (let [key :Leito.David.db.map/modify-layer-id]
              {:dispatch [:Leito.David.map-evs/tab-button :modify ]
               :db (merge db
                          {key ea})})))

(rf/reg-event-fx
 ::modify-layer-id
 (fn-traced [{:keys [db]} [_ ea]]
            (let [v ea
                  k :Leito.David.db.map/modify-layer-id]
              {:db (assoc db k v)
               :dispatch [:assoc-in-store [[k] v]]})))

(rf/reg-event-fx
 ::modify-wfs-click
 (fn-traced [{:keys [db]} [_ ea]]
            (let [{:keys [filter]} ea
                  ftype-input (:Leito.David.db.map/modify-layer-id db)
                  last-filter (:Leito.David.db.map/modify-wfs-click-last-filter db)
                  wfs-filter (or filter last-filter)
                  [fpref ftype] (try (str/split ftype-input \:)
                                     (catch js/Error e
                                       (do (js/console.warn e)
                                           ["undefined:undefined"])))
                  body (wfs-get-features-body-str
                        (merge
                         {:offset 0
                          :limit 100
                          :featurePrefix fpref
                          :featureTypes [ftype]}
                         (when wfs-filter
                           {:filter wfs-filter})))]
              #_(do (editor-request-set! (prettify-xml body)))
              {:dispatch [:Leito.David.evs/request-2
                          {:method :post
                           :params {}
                           :body body
                           :headers {"Content-Type" "application/json"}
                           :path "/geoserver/wfs?exceptions=application/json"
                           :response-format
                           (ajax/raw-response-format)
                           :expected-success-fmt :json->edn
                           :expected-failure-fmt :raw
                           :expected-body-fmt :xml
                           :on-success [::modify-wfs-click-res]
                           :on-failure [::modify-wfs-click-res]}]
               :db (merge db {:Leito.David.db.map/modify-wfs-click-last-filter wfs-filter})})))

(rf/reg-event-fx
 ::modify-wfs-click-res
 (fn-traced [{:keys [db]} [_ ea]]
            (let [fts (:features ea)]
              (merge
               {:db (-> db
                        (assoc :Leito.David.db.map/modify-wfs-click-res ea))}
               (when-not (empty? fts)
                 {:dispatch [::modified-features-add (first fts)]})))))

(rf/reg-event-fx
 ::modified-features-add
 (fn-traced [{:keys [db]} [_ ea]]
            (let [ft ea
                  k (:id ft)]
              (merge
               {:db (update-in db [:Leito.David.db.map/modified-features] assoc k ft)}))))

(rf/reg-event-fx
 ::modified-features-remove
 (fn-traced [{:keys [db]} [_ ea]]
            (let [k ea]
              (merge
               {:db (update-in db [:Leito.David.db.map/modified-features] dissoc k)}))))

(rf/reg-event-fx
 ::modified-features-selected-key
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (assoc db :Leito.David.db.map/modified-features-selected-key ea)}))

(rf/reg-event-fx
 ::cancel-modifying
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (-> db
                     (assoc  :Leito.David.db.map/modified-features {} )
                     )
             }))



(rf/reg-event-fx
 ::infer-feature-ns
 (fn-traced [{:keys [db]} [_ ea]]
            (let [
                  ftype-input (:Leito.David.db.map/modify-layer-id db)
                  [fpref ftype] (try (str/split ftype-input \:)
                                     (catch js/Error e
                                       (do (js/console.warn e)
                                           ["undefined:undefined"])))
                  path (str  "/geoserver/rest/namespaces/" fpref ".json")
                  ]
              {:dispatch [:Leito.David.evs/request-2
                          {:method :get
                           :params {}
                           :headers {"Content-Type" "application/json"}
                           :path path
                           :response-format
                           (ajax/raw-response-format)
                           :expected-success-fmt :json->edn
                           :expected-failure-fmt :raw
                           :expected-body-fmt :raw
                           :on-success [::infer-feature-ns-res]
                           :on-failure [::infer-feature-ns-res]}]
               :db db})))

(rf/reg-event-fx
 ::infer-feature-ns-res
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (assoc db :Leito.David.db.map/infer-feature-ns-res ea)
            ;  :dispatch [:assoc-in-store [[:Leito.David.db.map/infer-feature-ns-res] ea]]
             }))



(rf/reg-event-db
 ::modifying?
 (fn-traced [db [_ ea]]
            (assoc db :Leito.David.db.map/modifying? ea)))

(rf/reg-event-fx
 ::tx-features
 [(rf/inject-cofx :Leito.David.map-core/modify-features)]
 (fn-traced [{:keys [db modify-features]} [_ ea]]
            (let [ftype-input (:Leito.David.db.map/modify-layer-id db)
                  [fpref ftype] (try (str/split ftype-input \:)
                                     (catch js/Error e
                                       (do (js/console.warn e)
                                           ["undefined:undefined"])))
                  fns (get-in db [:Leito.David.db.map/infer-feature-ns-res :namespace :uri])
                  ; {:keys [updates]} ea
                  updates modify-features
                  body (wfs-tx-jsons-str
                        {:deletes nil
                         :inserts nil
                         :updates updates
                         :featureNS fns
                         :featurePrefix fpref
                         :featureType ftype})]
              {:dispatch [:Leito.David.evs/request-2
                          {:method :post
                           :body body
                           :headers {"Content-Type" "application/json"}
                           :path "/geoserver/wfs?exceptions=application/json"
                           :response-format
                           (ajax/raw-response-format)
                           :expected-success-fmt :xml
                           :expected-failure-fmt :xml
                           :expected-body-fmt :xml
                           :on-success [::tx-res-succ (str fpref ":" ftype)]
                           :on-failure [::tx-res-fail]}]
               :db (merge db {})})))

(rf/reg-event-fx
 ::tx-res-succ
 (fn-traced [{:keys [db]} [_ id ea]]
            {:dispatch [:Leito.David.map-core/refetch-wms-layer id]
             :db (merge db
                        {:Leito.David.db.map/tx-res ea
                         :Leito.David.db.map/modifying? false})}))

(rf/reg-event-db
 ::tx-res-fail
 (fn-traced [db [_ ea]]
            (assoc db :Leito.David.db.map/tx-res ea)))


(rf/reg-event-fx
 ::wfs-search-selected-key
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (assoc db :Leito.David.db.map/wfs-search-selected-key ea)}))


(rf/reg-event-fx
 ::wfs-tx
 (fn-traced [{:keys [db]} [_ ea]]
            (let [ftype-input (:Leito.David.db.map/wfs-search-layer-input db)
                  [fpref ftype] (try (str/split ftype-input \:)
                                     (catch js/Error e
                                       (do (js/console.warn e)
                                           ["undefined:undefined"])))
                  fns (get-in db [:Leito.David.db.map/wfs-search-fetch-ns-res
                                  :namespace :uri])
                  {:keys [value tx-type]} ea
                  v (js/JSON.parse value)
                  body (wfs-tx-jsons-str
                        {tx-type [v]
                         :featureNS fns
                         :featurePrefix fpref
                         :featureType ftype})]
              {:dispatch-n (list
                            [:Leito.David.evs/request-2
                             {:method :post
                              :body body
                              :headers {"Content-Type" "application/json"}
                              :path "/geoserver/wfs"
                              :response-format
                              (ajax/raw-response-format) #_(ajax/json-response-format {:keywords? true})
                              :expected-success-fmt :xml
                              :expected-failure-fmt :xml
                              :expected-body-fmt :xml
                              :on-success [::wfs-tx-succ (str fpref ":" ftype)]
                              :on-failure [::wfs-tx-fail]}]
                            #_[:Leito.David.feats-core/set-editor-xml [:request body]])
               :db (merge db {})})))

(rf/reg-event-fx
 ::wfs-tx-succ
 (fn-traced [{:keys [db]} [_ id ea]]
            {:db (assoc db :Leito.David.db.map/wfs-tx-res ea)
             :dispatch-n (list
                          #_[:Leito.David.feats-core/set-editor-xml [:response ea]]
                          [::wfs-search {}]
                          [:Leito.David.map-core/refetch-wms-layer id])}))


(rf/reg-event-fx
 ::wfs-tx-fail
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (assoc db :Leito.David.db.map/wfs-tx-res ea)
            ;  :dispatch [:Leito.David.feats-core/set-editor-xml [:response ea]]
             }))


(rf/reg-event-fx
 ::wfs-search-fetch-ns
 (fn-traced [{:keys [db]} [_ ea]]
            (let [ftype-input (:Leito.David.db.map/wfs-search-layer-input db)
                  [fpref ftype] (try (str/split ftype-input \:)
                                     (catch js/Error e
                                       (do (js/console.warn e)
                                           ["undefined:undefined"])))]
              {:dispatch-n (list
                            [:Leito.David.evs/request-2
                             {:method :get
                              :headers {"Content-Type" "application/json"}
                              :path (str "/geoserver/rest/namespaces/" fpref ".json")
                              :response-format
                              (ajax/raw-response-format)
                              :expected-success-fmt :json->edn
                              :expected-failure-fmt :raw
                              :expected-body-fmt :raw
                              :on-success [::wfs-search-fetch-ns-res]
                              :on-failure [::wfs-search-fetch-ns-res]}])})))

(rf/reg-event-fx
 ::wfs-search-fetch-ns-res
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (assoc db :Leito.David.db.map/wfs-search-fetch-ns-res ea)}))

(rf/reg-event-fx
 ::modify-mode
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (assoc db :Leito.David.db.map/modify-mode ea)}))
