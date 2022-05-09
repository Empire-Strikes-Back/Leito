(ns Leito.David.feats-evs
  (:require [re-frame.core :as rf]
            [clojure.repl :as repl]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
            [Leito.David.wfs :refer [wfs-tx-jsons-str wfs-get-features-body-str]]
            [ajax.core :as ajax]
            [clojure.string :as str]
            [Leito.David.core :refer [url-search-params]]
            [Leito.David.wfs :refer [attrs->eqcl-ilike]]
            ["ol/format/filter" :as olf]))

#_(repl/dir xml)

#_(xml/indent)

#_(str/replace "@input" #"@input" "3")
#_(str/replace "The color is red" #"red" "blue")


(rf/reg-event-fx
 ::search
 [(rf/inject-cofx :Leito.David.feats-core/get-editor-val [:ecql])]
 (fn-traced [{:keys [db get-editor-val]} [_ ea]]
   (let [ftype-input (:Leito.David.db.feats/feature-type-input db)
         [fpref ftype] (try (str/split ftype-input \:)
                            (catch js/Error e
                              (do (js/console.warn e)
                                  ["undefined:undefined"])))
         input (:Leito.David.db.feats/search-input db)
         s (or (:input ea) input)
         table-mdata (:Leito.David.db.feats/search-table-mdata db)
         total (get-in db [:Leito.David.db.feats/search-res :total])
         pag (:pagination table-mdata)
         use-eqcl-filter? (:Leito.David.db.feats/use-eqcl-filter? db)
         ecql-filter (str/replace get-editor-val #"@input" s)
         {:keys [current pageSize]} pag
         limit (or pageSize 10)
         offset (or (* pageSize (dec current)) 0)
         body (wfs-get-features-body-str
               (merge
                {:offset offset
                 :limit limit
                 :featurePrefix fpref
                 :featureTypes [ftype]}
                (when (not-empty s)
                  {:filter
                   #_(str "NAME ILIKE " s)
                   (olf/like "NAME" (str "*" s "*") "*" "." "!" false)})))
         selected-attrs (:Leito.David.db.feats/selected-attrs db)
         attrs (get-in db [:Leito.David.db.feats/fetch-ftype-mdata-layer-res
                           :featureType :attributes :attribute])
         attrs-flt (filterv (fn [x]
                              (some #(= % (:name x)) selected-attrs)) attrs)]
     #_(js/console.log (olf/like "NAME" (str "*" "hello" "*") "*" "." "!" false))
     {:dispatch-n (list 
                   [:Leito.David.evs/request-2
                    {:method :get
                     :path "/geoserver/wfs"
                     :params (merge
                              {"service" "wfs"
                              ;  "version" "2.0.0"
                               "version" "1.1.0"
                               "request" "GetFeature"
                               "srsName" "EPSG:3857"
                               "count" limit
                               "startIndex" offset
                               "typeNames" ftype-input
                               "exceptions" "application/json"
                               "maxFeatures" limit
                               "outputFormat" "application/json"}
                              (when (and (not use-eqcl-filter?) (not (empty? attrs-flt)) (not (empty? s)))
                                {"cql_filter" (attrs->eqcl-ilike {:attrs attrs-flt
                                                                  :input s
                                                                  :joiner "OR"})})
                              (when use-eqcl-filter?
                                {"cql_filter" ecql-filter})
                              #_(when-not (empty? s)
                                  {"cql_filter" (str "NAME ilike \n '%" s "%'")}))
                     :headers {}
                     :response-format 
                     #_(ajax/raw-response-format)
                     (ajax/json-response-format {:keywords? true})
                     
                     :expected-success-fmt :json->edn
                     :expected-failure-fmt :raw
                     :expected-body-fmt :json
                     :on-success [::search-res]
                     :on-failure [::search-res]}
                    ]
                   )}
     
     #_{:dispatch-n (list
                 [:Leito.David.evs/request
                  {:method :post
                   :params {}
                   :body body
                   :headers {"Content-Type" "application/json"}
                   :path "/geoserver/wfs"
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [::search-res]
                   :on-failure [::search-res]}]
                 [:Leito.David.feats-core/set-editor-xml [:request body]])
      :db (merge db {:Leito.David.db.feats/search-input s
                     :Leito.David.db.feats/search-table-mdata
                     (if (:input ea)
                       (merge table-mdata {:pagination (merge pag {:current 1})})
                       table-mdata)})})))

(rf/reg-event-db
 ::search-res
 (fn-traced [db [_ ea]]
            (assoc db :Leito.David.db.feats/search-res ea)))

(rf/reg-event-fx
 ::tx-feature
 [(rf/inject-cofx :Leito.David.feats-core/get-editor-val [:data])]
 (fn-traced [{:keys [db get-editor-val]} [_ ea]]
            (let [ftype-input (:Leito.David.db.feats/feature-type-input db)
                  [fpref ftype] (try (str/split ftype-input \:)
                                     (catch js/Error e
                                       (do (js/console.warn e)
                                           ["undefined:undefined"])))
                  fns (get-in db [:Leito.David.db.feats/fetch-ftype-mdata-ns-res
                                  :namespace :uri])
                  tx-type (:tx-type ea)
                  v (js/JSON.parse get-editor-val)
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
                              :on-success [::tx-res-succ (str fpref ":" ftype)]
                              :on-failure [::tx-res-fail]}]
                            #_[:Leito.David.feats-core/set-editor-xml [:request body]])
               :db (merge db {})})))

(rf/reg-event-fx
 ::tx-res-succ
 (fn-traced [{:keys [db]} [_ id ea]]
            {:db (assoc db :Leito.David.db.feats/tx-res ea)
             :dispatch-n (list
                          #_[:Leito.David.feats-core/set-editor-xml [:response ea]]
                          [:Leito.David.map-core/refetch-wms-layer id])}))


(rf/reg-event-fx
 ::tx-res-fail
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (assoc db :Leito.David.db.feats/tx-res ea)
            ;  :dispatch [:Leito.David.feats-core/set-editor-xml [:response ea]]
             }))

(rf/reg-event-fx
 ::search-input
 (fn-traced [{:keys [db]} [_ ea]]
            (let [k :Leito.David.db.feats/search-input
                  v ea]
              {:db (assoc db k v)
               })))

(rf/reg-event-fx
 ::search-table-mdata
 (fn-traced [{:keys [db]} [_ ea]]
   (let [key :Leito.David.db.feats/search-table-mdata]
     {:dispatch [:Leito.David.feats-evs/search {}]
      :db (assoc db key ea)})))

(rf/reg-event-fx
 ::select-feature
 (fn-traced [{:keys [db]} [_ ea]]
            (let [key :Leito.David.db.feats/select-feature]
              {:db (assoc db key ea)
               :dispatch [:Leito.David.feats-core/set-editor-json [:data ea]]})))

(rf/reg-event-fx
 ::feature-type-input
 (fn-traced [{:keys [db]} [_ ea]]
   (let [v ea
         k :Leito.David.db.feats/feature-type-input]
     {:db (assoc db k v)
      :dispatch [:assoc-in-store [[k] v]]})))

(rf/reg-event-fx
 ::feature-ns
 (fn-traced [{:keys [db]} [_ ea]]
            (let [k :Leito.David.db.feats/feature-ns]
              {:db (assoc db k ea)
               :dispatch [:assoc-in-store [[k] ea]]})))

(rf/reg-event-fx
 ::fetch-ftype-mdata
 (fn-traced [{:keys [db ]} [_ ea]]
            (let [ftype-input (:Leito.David.db.feats/feature-type-input db)
                  [fpref ftype] (try (str/split ftype-input \:)
                                     (catch js/Error e
                                       (do (js/console.warn e)
                                           ["undefined:undefined"])))
                  ]
              {:dispatch-n (list 
                            [:Leito.David.evs/request-2
                             {:method :get
                              :headers {"Content-Type" "application/json"}
                              :path (str "/geoserver/rest/workspaces/" fpref "/featuretypes/" ftype ".json")
                              :response-format
                              (ajax/raw-response-format)
                              :expected-success-fmt :json->edn
                              :expected-failure-fmt :raw
                              :expected-body-fmt :raw
                              :on-success [::fetch-ftype-mdata-layer-res]
                              :on-failure [::fetch-ftype-mdata-layer-res]}]
                            [:Leito.David.evs/request-2
                             {:method :get
                              :headers {"Content-Type" "application/json"}
                              :path (str "/geoserver/rest/namespaces/" fpref ".json")
                              :response-format
                              (ajax/raw-response-format)
                              :expected-success-fmt :json->edn
                              :expected-failure-fmt :raw
                              :expected-body-fmt :raw
                              :on-success [::fetch-ftype-mdata-ns-res]
                              :on-failure [::fetch-ftype-mdata-ns-res]}]
                            )
               })))

(rf/reg-event-fx
 ::fetch-ftype-mdata-layer-res
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (assoc db :Leito.David.db.feats/fetch-ftype-mdata-layer-res ea)}))

(rf/reg-event-fx
 ::fetch-ftype-mdata-ns-res
 (fn-traced [{:keys [db]} [_ ea]]
            {:db (assoc db :Leito.David.db.feats/fetch-ftype-mdata-ns-res ea)}))


(rf/reg-event-fx
 ::selected-attrs
 (fn-traced [{:keys [db]} [_ ea]]
            (let [v ea]
              {:db (assoc db :Leito.David.db.feats/selected-attrs v)})
            ))

(rf/reg-event-fx
 ::use-eqcl-filter?
 (fn-traced [{:keys [db]} [_ ea]]
            (let [v ea]
              {:db (assoc db :Leito.David.db.feats/use-eqcl-filter? v)})))