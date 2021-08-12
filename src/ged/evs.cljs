(ns ged.evs
  (:require
   [clojure.string :as str]
   [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path after] :as rf]
   [day8.re-frame.http-fx]
   [ged.db :refer [default-db]]
   #_[vimsical.re-frame.cofx.inject :as inject]
   [ajax.core :as ajax]
   [ajax.edn]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [ged.core :refer [basic-creds]]
   ["antd/lib/message" :default AntMessage]
   [ged.storage]
   [ged.core :refer  [deep-merge]]
   [ged.storage :as storage]))


#_(defn my-reg-event-db            ;; a replacement for reg-event-db

   ;; 2-arity with no interceptors 
  ([id handler]
   (my-reg-event-db id nil handler))

   ;; 3-arity with interceptors
  ([id interceptors handler]
   (re-frame.core/reg-event-db   ;; which uses reg-event-db 
    id
    [omni-ceptor interceptors] ;; <-- inject `omni-ceptor`
    handler)))

; core

(rf/reg-event-fx
 ::initialize-db
 [(inject-cofx :stored-db)]
 (fn-traced [{:keys [db stored-db] :as coef} av]
            {:db (merge
                  ged.db/default-db
                  stored-db
                  {:ged.db.core/profiles
                   (merge
                    (:ged.db.core/profiles ged.db/default-db)
                    (:ged.db.core/profiles stored-db))
                   :ged.db.core/active-profile-key
                   (or
                    (:ged.db.core/active-profile-key stored-db)
                    (:ged.db.core/active-profile-key ged.db/default-db))}
                  )}))

(rf/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
            (assoc db :ged.db.core/active-panel active-panel)))

(rf/reg-event-fx
 :ant-message
 (fn-traced [{:keys [db]} [_ {:keys [msg dur]}]]
            (let []
              (.info AntMessage msg (or dur 0.5))
              {})))

(rf/reg-event-db
 ::set-re-pressed-example
 (fn-traced [db [_ value]]
            (assoc db :re-pressed-example value)))

(rf/reg-event-db
 ::inc-module-count
 (fn-traced [db [_ active-panel]]
            (let [kw :ged.db.core/module-count]
              (assoc db kw (inc (kw db))))))

(rf/reg-event-fx
 ::apply-server-settings
 (fn-traced [{:keys [db]} [_ ea]]
            (let [apk (:ged.db.core/active-profile-key db)
                  proxy-host (get-in db [:ged.db.core/profiles apk :proxy-host])
                  proxy-path (:ged.db.core/proxy-path db)
                  body (str  {:proxy-geoserver-host proxy-host
                              :proxy-path proxy-path})]
              {:dispatch [:ged.evs/request
                          {:method :post
                           :body body
                           :headers {"Content-Type" "application/json" #_"text/html; charset=utf-8"}
                  ; :path "/geoserver/wfs?exceptions=application/json&outputFormat=application/json"
                           :path "/update-settings"
                           :response-format
                           (ajax/raw-response-format)
                  ; (ajax/json-response-format {:keywords? true})
                           :on-success [::apply-server-settings-res]
                           :on-failure [::apply-server-settings-res]}]
               :db (merge db {})})))

(rf/reg-event-fx
 ::apply-server-settings-res
 (fn-traced [{:keys [db]} [_ ea]]
            {
            ;  :dispatch [:ant-message {:msg "applied"}]
             
             }))

; edn deprecated
; https://github.com/JulianBirch/cljs-ajax/blob/master/docs/formats.md#edn
(rf/reg-event-fx
 ::request
;  [(re-frame/)]
 ;[(rf/inject-cofx ::inject/sub [:entity-request-data])]
 (fn-traced [{:keys [db event] :as ctx} [_ ea]]
            (let [base-url (get-in db [:ged.db.core/api :base-url])
                  {:keys [method path on-success on-failure
                          params url-params body headers response-format]} ea
                  uri (str base-url path)
                  proxy-path (:ged.db.core/proxy-path db)
                  geoserver-req? (str/starts-with? uri proxy-path)
                  apk (:ged.db.core/active-profile-key db)
                  uname (get-in db [:ged.db.core/profiles apk :username])
                  pass (get-in db [:ged.db.core/profiles apk :password])]
              {:http-xhrio {:method method
                            :uri uri
                  ;  :response-format (ajax.edn/edn-response-format)
                            :response-format (or response-format (ajax/json-response-format {:keywords? true}))
                            #_(ajax/raw-response-format)
                            :on-success on-success
                            :format :edn
                            :body body
                            :headers (merge headers
                                            (when geoserver-req?
                                              {"Authorization" (basic-creds uname pass)}))
                  ;  :params {:data "{:hello 'world}"}
                            :params params
                            :url-params url-params
                            :on-failure on-failure}}
     ;
              )))

(rf/reg-event-fx
 ::request-2
;  [(re-frame/)]
 ;[(rf/inject-cofx ::inject/sub [:entity-request-data])]
 (fn-traced [{:keys [db event] :as ctx} [_ ea]]
            (let [base-url (get-in db [:ged.db.core/api :base-url])
                  {:keys [method path
                          on-success on-failure
                          expected-success-fmt expected-failure-fmt
                          expected-body-fmt 
                          params url-params body headers response-format]} ea
                  uri (str base-url path)
                  proxy-path (:ged.db.core/proxy-path db)
                  geoserver-req? (str/starts-with? uri proxy-path)
                  apk (:ged.db.core/active-profile-key db)
                  uname (get-in db [:ged.db.core/profiles apk :username])
                  pass (get-in db [:ged.db.core/profiles apk :password])

                  http-xhrio {:method method
                              :uri uri
                              :response-format (ajax/raw-response-format)

                              :format :edn
                              :body body
                              :headers
                              (merge headers
                                     (when geoserver-req?
                                       {"Authorization" (basic-creds uname pass)}))

                              :params params
                              :url-params url-params
                              }

                  on-success [::request-2-success {:on-success on-success
                                                   :http-xhrio http-xhrio
                                                   :expected-body-fmt expected-body-fmt
                                                   :expected-success-fmt expected-success-fmt}]
                  on-failure [::request-2-failure {:on-failure on-failure
                                                   :http-xhrio http-xhrio
                                                   :expected-body-fmt expected-body-fmt
                                                   :expected-failure-fmt expected-failure-fmt}]]
              {:http-xhrio [(merge http-xhrio {:on-failure on-failure
                                               :on-success on-success} )]}
     ;
              )))

(rf/reg-event-fx
 ::request-2-success
 (fn [{:keys [db]} [_ {:keys [on-success expected-success-fmt http-xhrio
                              expected-body-fmt]} v]]
   (let [nv (cond
              (= expected-success-fmt :json) (->  v (js/JSON.parse))
              (= expected-success-fmt :json->edn) (->  v (js/JSON.parse) (js->clj :keywordize-keys true))
              (= expected-success-fmt :xml) v
              (= expected-success-fmt :raw) v
              :else v)]
     {:dispatch-n (list 
                   (conj on-success nv)
                   [::log {:uuid (str (random-uuid))
                           :response v
                           :http-xhrio http-xhrio
                           :expected-body-fmt expected-body-fmt
                           :expected-success-fmt expected-success-fmt}]
                   )})))

(rf/reg-event-fx
 ::request-2-failure
 (fn [{:keys [db]} [_ {:keys [on-failure expected-failure-fmt expected-body-fmt
                              http-xhrio]} v]]
   {:dispatch [::log {:uuid (str (random-uuid))
                      :result v
                      :http-xhrio http-xhrio
                      :expected-body-fmt expected-body-fmt
                      :expected-failure-fmt expected-failure-fmt}]}))


(rf/reg-event-fx
 :xhrio-failure
 (fn [{:keys [db]} [_ ea]]
   (js/console.log ":xhrio-failure")
   (js/console.log ea)
   {}))

(rf/reg-event-db
 :http-no-on-failure
 (fn [db [_ ea]]
   (js/console.warn ":http-no-on-failure event "
                    ea)
   db))

(rf/reg-event-db
 :request-res
 (fn-traced [db [_ db-key res]]
            (assoc db db-key res)))

(rf/reg-event-fx
 ::log
 (fn-traced
  [{:keys [db]} [_ ea]]
  {:db (update db :ged.db.core/log-que conj
               (assoc ea :ts-created (js/Date.now)))}))

(rf/reg-event-fx
 ::clear-log
 (fn-traced
  [{:keys [db]} [_ ea]]
  {:db (assoc db :ged.db.core/log-que [])}))



; local storage


(def default-active-profile-key 0)

(rf/reg-cofx
 :stored-db
 (fn [{:keys [db] :as cofx} ea]
   (let [stored-db  (storage/read-db)
         apk (or (:active-profile-key stored-db) default-active-profile-key)
         profile-dbs  (:profile-dbs stored-db)
         profiles  (:profiles stored-db)
         profile-db (get-in stored-db [:profile-dbs apk])]
     (assoc cofx :stored-db
            (merge db
                   profile-db
                   {:ged.db.core/profiles profiles
                    :ged.db.core/active-profile-key apk})))))

(rf/reg-event-fx
 :assoc-in-store
 (fn-traced [{:keys [db]} [_ ea]]
            (do
              (let [[path v] ea
                    apk (:ged.db.core/active-profile-key db)
                    combined-path (into [:profile-dbs apk] path)]
                (storage/assoc-in-store! combined-path v)))
            {}))



; profiles

(rf/reg-event-fx
 ::add-profile
 (fn-traced
  [{:keys [db]} [_ ea]]
  (let [pfs (:ged.db.core/profiles db)
        k (->> pfs keys (apply max) inc)
        pf {:key k
            :host "http://geoserver:8080/geoserver"
            :proxy-host "http://localhost:8600/geoserver"
            :username "admin"
            :password "geoserver"}
        pfs (assoc (:ged.db.core/profiles db) k pf)]
    (do
      (storage/assoc-in-store! [:profiles] pfs))
    {:db (assoc db :ged.db.core/profiles pfs)})))

(rf/reg-event-fx
 ::remove-profile
 (fn-traced
  [{:keys [db]} [_ ea]]
  (let [k (aget ea "key")
        pfs (dissoc (:ged.db.core/profiles db) k)]
    (do
      (storage/assoc-in-store! [:profiles] pfs))
    {:db (assoc db :ged.db.core/profiles pfs)})))

(rf/reg-event-fx
 ::update-profile
 (fn-traced
  [{:keys [db]} [_ ea]]
  (let [k (:key ea)
        v (:ged.db.core/profiles db)
        pfs (update-in v [k] merge ea)]
    (do
      (storage/assoc-in-store! [:profiles] pfs))
    {:db (assoc db :ged.db.core/profiles pfs)})))



(rf/reg-event-fx
 ::activate-profile
 (fn-traced
  [{:keys [db]} [_ ea]]
  (let [apk (aget ea "key")
        profile-db (storage/read-profile-db apk)
        profiles (storage/read-profiles)]
    (do (storage/assoc-in-store! [:active-profile-key] apk))
    {:db (merge
          db
          (or profile-db ged.db/default-db)
          {:ged.db.core/active-profile-key apk
           :ged.db.core/profiles (merge (:ged.db.core/profiles db) profiles)})
     :dispatch [:ged.evs/apply-server-settings]})))

(rf/reg-event-fx
 ::update-profiles
 (fn-traced
  [{:keys [db]} [_ ea]]
  (let [pfs (deep-merge (:ged.db.core/profiles db) ea)]
    (do
      (storage/assoc-in-store! [:profiles] pfs))
    {:db (assoc-in db [:ged.db.core/profiles] pfs)
     :dispatch-n (list [:ant-message {:msg "profiles updated" :dur 1}])})))