(ns Leito.David.req
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [Leito.David.core :refer [deep-merge]]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

; https://developer.mozilla.org/en-US/docs/Web/API/AbortController
; https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API


(defn f?
  [ctx v]
  (if (fn? v) (v ctx) v))

(defn merge-with-rules
  [ctx rules & maps]
  (when (some identity maps)
    (let [merge-entry (fn [m e]

                        (let [k (key e)
                              v (val e)
                              f (get rules k)]
                          (if (contains? m k)
                            (assoc m k (f ctx [k (get m k) v] ))
                            (assoc m k v))))
          merge2 (fn [m1 m2]
                   (reduce merge-entry (or m1 {}) (seq m2)))]
      (reduce merge2 maps))))

#_(let [rules {:url (fn furl [ctx [k a b]]
                      (js/console.log ctx)
                      (str (f? ctx a) (f? ctx b)))}
        m1 {:url (fn [] "/geoserver")}
        m2 {:url "/wfs"}]
    (merge-with-rules {:db {}} rules m1 m2))

#_(fn? :url)
#_(fn? (fn [] "" ))
#_(fn? "")

#_(merge-with (fn [a b] (js/console.log a b)) {:a 1} {:a 2}  )
#_(reduce (fn [a v] (js/console.log v) a ) {} (seq {:a 1}) )
#_(apply + (list 1 2))

; registry for multistep requests
; register via uuids 
(def registry (atom {}))

(def http-conf
  {:merge-rules {:url (fn [cofx [k a b]] (str (f? cofx a) (f? cofx b)))
                 :profiles (fn [c [_ a b]] (into a b))
                 :on-success (fn [c [_ a b]] (into a b))
                 :on-failure (fn [c [_ a b]] (into a b))
                 :headers (fn [c [_ a b]] (merge a b))
                 }
   :requests {:wfs-search {:method :post
                           :body (fn [args]
                                   {}
                                   ) }
              
              }
   :profiles {:json {:default? true
                     :headers {"Content-Type" "application/json"}}
              :proxy-path
              {:url (fn [{:keys [db]}] (:Leito.David.db.core/proxy-path db))}

              :wfs-get-feature
              {:profiles [:json :proxy-path]
               :url "/wfs"
               :on-success [:Leito.David.log-evs/http-on-success]
               :on-failure [:Leito.David.log-evs/http-on-failure]
               :requires [:gs.rest.ns/id]
               }
              :geoserver-rest {}}
   
   :resources {:gs.rest.ns/id {:resolve-http (fn [ctx])
                               :resolve-db (fn [{:keys [db]} [id _]]
                                             (get-in db [:Leito.David.db.core/namespaces id]))
                               :expire 10000
                               :requires []
                               }}
   })

; register requests by id (via event)
; async resolve (fetch) resources
; when response arrives, dispatch continuation event for the registered id
; the graph (or tree) of resources resolve themselves, finally proceeding with inital request


(defn reverse-distinct
  [v]
  (-> v reverse distinct reverse))

(defn profile->keys
  [profile profiles]
  (->>
   (reduce (fn [a k]
             (into a (profile->keys (k profiles) profiles)))
           (:profiles profile) (:profiles profile))
   (reverse-distinct)
   (vec)))

(defn combined-profile
  [cofx rq profiles]
  (let [ks (profile->keys rq profiles)
        pfs (mapv #(% profiles) ks)
        rules (get-in cofx [:http-conf :merge-rules])]
    (apply (partial merge-with-rules cofx rules rq) pfs)))

#_(map (fn [e] (js/console.log e) ) {:a 1} )
#_(distinct [:b :c :a :b :a :b])
#_(-> [:b :c :a :b :a :b] reverse distinct reverse vec)


(rf/reg-cofx
 :http-conf
 (fn [cofx ea]
   (assoc cofx :http-conf http-conf)))

(rf/reg-event-fx
 ::request
 [(rf/inject-cofx :http-conf)]
 (fn-traced
  [{:keys [db http-conf] :as cofx} [_ ea]]
  (let [pf (combined-profile cofx ea (:profiles http-conf))]
    (js/console.log pf)
    #_(do
        (->
         (js/fetch (:url ea)
                   (:init ea))
         (.catch (fn [e]
                   (rf/dispatch (:on-failure ea))))
         (.then (fn [r]
                  (rf/dispatch (:on-success ea))))))
    {})))



