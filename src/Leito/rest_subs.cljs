(ns Leito.rest-subs
  (:require [re-frame.core :as rf]))



(rf/reg-sub
 ::fetch-selected-url-list
 (fn [db _]
   (let [res (:Leito.db.rest/fetch-selected-url-res db)]
     (cond
       (:featureTypes res)
       (get-in res [:featureTypes :featureType])))))

(rf/reg-sub
 ::selected-url
 (fn [db _]
   (:Leito.db.rest/selected-url db)))

(rf/reg-sub
 ::layer-id-input
 (fn [db _]
   (:Leito.db.rest/layer-id-input db)))


(rf/reg-sub
 ::search-table-mdata
 (fn [db _]
   (:Leito.db.rest/search-table-mdata db)))



