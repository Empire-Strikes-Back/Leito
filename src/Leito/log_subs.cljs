(ns Leito.log-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::log
 (fn [db _]
   (let [data (:Leito.db.core/log-que db)
         pag (get-in db [:Leito.db.log/log-table-mdata :pagination])
         {:keys [current pageSize]} pag]
     {:total (count data)
      :data (->> data
                 (reverse)
                 (drop (* (dec current) pageSize))
                 (take pageSize)
                 (vec))})))

(rf/reg-sub
 ::log-table-mdata
 (fn [db _]
   (:Leito.db.log/log-table-mdata db)))

(rf/reg-sub
 ::selected-item-key
 (fn [db _]
   (:Leito.db.log/selected-item-key db)))

(rf/reg-sub
 ::selected-item
 (fn [query-v _]
   [(rf/subscribe [::log])
    (rf/subscribe [::selected-item-key])])
 (fn [[log k] qv _]
   (let []
     (->> (:data log)
          (filterv #(= (:uuid %) k))
          (first)))))