(ns Leito.feats-subs
  (:require [re-frame.core :as rf]))



(rf/reg-sub
 ::search-res
 (fn [db _]
   (:Leito.db.feats/search-res db)))

(rf/reg-sub
 ::search-input
 (fn [db _]
   (:Leito.db.feats/search-input db)))

(rf/reg-sub
 ::search-table-mdata
 (fn [db _]
   (:Leito.db.feats/search-table-mdata db)))

(rf/reg-sub
 ::feature-type-input
 (fn [db _]
   (:Leito.db.feats/feature-type-input db)))

(rf/reg-sub
 ::feature-ns
 (fn [db _]
   (get-in db [:Leito.db.feats/fetch-ftype-mdata-ns-res
               :namespace :uri])
   #_(:Leito.db.feats/feature-ns db)))

(rf/reg-sub
 ::layer-attributes
 (fn [db _]
   (get-in db [:Leito.db.feats/fetch-ftype-mdata-layer-res
               :featureType :attributes :attribute])))

(rf/reg-sub
 ::selected-attrs
 (fn [db _]
   (:Leito.db.feats/selected-attrs db)))

(rf/reg-sub
 ::use-eqcl-filter?
 (fn [db _]
   (:Leito.db.feats/use-eqcl-filter? db)))
