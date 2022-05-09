(ns Leito.David.rest-evs
  (:require [clojure.repl :as repl]
            [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
            [ajax.core :as ajax]
            [clojure.string :as str]))

#_(repl/dir xml)

#_(xml/indent)

(rf/reg-event-db
 ::selected-url
 (fn-traced [db [_ ea]]
            (assoc db :Leito.David.db.rest/selected-url ea)))

(rf/reg-event-fx
 ::fetch-selected-url
 (fn-traced [{:keys [db]} [_ ea]]
            (let [selected-url (:Leito.David.db.rest/selected-url db)]
              {:dispatch [:Leito.David.evs/request
                          {:method :get
                           :params {}
                           :headers {"Content-Type" "application/json"
                            ; "Authorization"  (Leito.David.api.geoserver/auth-creds)
                                     }
                           :path (str "/geoserver" selected-url)
                           :response-format
                           (ajax/json-response-format {:keywords? true})
                           #_(ajax/raw-response-format)
                           :on-success [::fetch-selected-url-res]
                           :on-failure [::fetch-selected-url-res]}]
               :db (merge db {})})))

(rf/reg-event-db
 ::fetch-selected-url-res
 (fn-traced [db [_ ea]]
            (assoc db :Leito.David.db.rest/fetch-selected-url-res ea)))

(rf/reg-event-fx
 ::tx-item
 [(rf/inject-cofx :Leito.David.rest-core/get-editor-val [:data])]
 (fn-traced [{:keys [db get-editor-val]} [_ ea]]
            (let [tx-type (:tx-type ea)
                  v (js/JSON.parse get-editor-val)
                  path (:Leito.David.db.rest/selected-item-path db)
                  body (js/JSON.stringify v)]
              {:dispatch [:Leito.David.evs/request
                          {:method tx-type
                           :body body
                           :headers {"Content-Type" "application/json"
                            ; "Authorization"  (Leito.David.api.geoserver/auth-creds)
                                     }
                  ; :path "/geoserver/wfs?exceptions=application/json&outputFormat=application/json"
                           :path (str "/geoserver/rest" path)
                           :response-format
                           #_(ajax/raw-response-format)
                           (ajax/json-response-format {:keywords? true})
                           :on-success [::tx-res]
                           :on-failure [::tx-res]}]
               :db (merge db {})})))

(rf/reg-event-fx
 ::tx-res
 (fn-traced [{:keys [db]} [_ id ea]]
            {:db (assoc db :Leito.David.db.rest/tx-res ea)
             :dispatch [:Leito.David.rest-core/set-editor-json [:response ea]]}))


(rf/reg-event-db
 ::search-input
 (fn-traced [db [_ ea]]
            (let [key :Leito.David.db.rest/search-input
                  value ea]
              (assoc db key value))))

(rf/reg-event-db
 ::layer-id-input
 (fn-traced [db [_ ea]]
            (assoc db :Leito.David.db.rest/layer-id-input ea)))


(rf/reg-event-fx
 ::search-table-mdata
 (fn-traced [{:keys [db]} [_ ea]]
            (let [key :Leito.David.db.rest/search-table-mdata]
              {:dispatch-n (list nil [:Leito.David.rest-events/search {}])
               :db (assoc db key ea)})))

(defn href->path
  [href]
  (-> (str/split href "/geoserver/rest") (second)))

(rf/reg-event-fx
 ::select-feature
 (fn-traced [{:keys [db]} [_ ea]]
   (let [href (:href (js->clj ea :keywordize-keys true))]
     (if href
       (let [path (href->path href)]
         {:dispatch [:Leito.David.evs/request
                     {:method :get
                      :headers {"Content-Type" "application/json"
                            ; "Authorization"  (Leito.David.api.geoserver/auth-creds)
                                }
                      :path (str "/geoserver/rest" path)
                      :response-format
                      #_(ajax/raw-response-format)
                      (ajax/json-response-format {:keywords? true})
                      :on-success [::select-feature-succ]
                      :on-failure [::select-feature-fail]}]
          :db (merge db {:Leito.David.db.rest/selected-item-href href
                         :Leito.David.db.rest/selected-item-path path})})
       {:dispatch [:Leito.David.rest-core/set-editor-json [:data ea]]
        :db (assoc db :Leito.David.db.rest/select-feature ea)})
     )))

(rf/reg-event-fx
 ::select-feature-succ
 (fn-traced [{:keys [db]} [_ ea]]
            {:dispatch [:Leito.David.rest-core/set-editor-json [:data (clj->js ea)]]}))

(rf/reg-event-fx
 ::select-feature-fail
 (fn-traced [{:keys [db]} [_ ea]]
            {:dispatch [:Leito.David.rest-core/set-editor-json [:response (clj->js ea)]]}))

#_(str/split 
   "http://localhost:8800/geoserver/rest/workspaces/dev/featuretypes/ne_10m_admin_0_countries.json"
   "/geoserver"
   )

(rf/reg-event-fx
 ::fetch-layer
 (fn-traced [{:keys [db]} [_ ea]]
            (let [id (:Leito.David.db.rest/layer-id-input db)
                  [fpref ftype] (try (str/split id \:)
                                     (catch js/Error e
                                       (do (js/console.warn e)
                                           ["undefined:undefined"])))]
              {:dispatch [:Leito.David.evs/request-2
                          {:method :get
                           :params {}
                           :headers {"Content-Type" "application/json"
                            ; "Authorization"  (Leito.David.api.geoserver/auth-creds)
                                     }
                           :path (str "/geoserver/rest/workspaces/" fpref "/featuretypes/" ftype ".json")
                           :response-format
                           (ajax/raw-response-format)
                           :expected-success-fmt :json
                           :expected-failure-fmt :raw
                           :expected-body-fmt :json
                           :on-success [::fetch-layer-res]
                           :on-failure [::fetch-layer-res]}]
               :db (merge db {})})))

(rf/reg-event-fx
 ::fetch-layer-res
 (fn-traced [{:keys [db]} [_ ea]]
            {:dispatch [:Leito.David.rest-core/set-editor-json [:data ea]]}))


(rf/reg-event-fx
 ::tx-item-2
 [(rf/inject-cofx :Leito.David.rest-core/get-editor-val [:data])]
 (fn-traced [{:keys [db get-editor-val]} [_ ea]]
            (let [tx-type (:tx-type ea)
                  v (js/JSON.parse get-editor-val)
                  path (:Leito.David.db.rest/selected-item-path db)
                  body (js/JSON.stringify v)
                  id (:Leito.David.db.rest/layer-id-input db)
                  [fpref ftype] (try (str/split id \:)
                                     (catch js/Error e
                                       (do (js/console.warn e)
                                           ["undefined:undefined"])))]
              {:dispatch [:Leito.David.evs/request-2
                          {:method tx-type
                           :body body
                           :headers {"Content-Type" "application/json"
                            ; "Authorization"  (Leito.David.api.geoserver/auth-creds)
                                     }
                  ; :path "/geoserver/wfs?exceptions=application/json&outputFormat=application/json"
                           :path (if (= tx-type :post)
                                   (str "/geoserver/rest/workspaces/" fpref "/featuretypes" )
                                   (str "/geoserver/rest/workspaces/" fpref "/featuretypes/" ftype ".json")
                                   )
                           :response-format
                           (ajax/raw-response-format)
                           #_(ajax/json-response-format {:keywords? true})
                           :expected-success-fmt :raw
                           :expected-failure-fmt :raw
                           :expected-body-fmt :json
                           :on-success [::tx-res-2]
                           :on-failure [::tx-res-2]}]
               :db (merge db {})})))

(rf/reg-event-fx
 ::tx-res-2
 (fn-traced [{:keys [db]} [_ id ea]]
            {:db (assoc db :Leito.David.db.rest/tx-res ea)}))