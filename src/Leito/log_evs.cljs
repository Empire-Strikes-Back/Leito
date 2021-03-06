(ns Leito.log-evs
  (:require [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
            [goog.dom]
            [Leito.log-core]))



(rf/reg-event-fx
 ::log-table-mdata
 (fn-traced [{:keys [db]} [_ ea]]
            (let [k :Leito.db.log/log-table-mdata]
              {:db (assoc db k ea)})))

(rf/reg-event-fx
 ::select-item
 (fn-traced [{:keys [db]} [_ ea]]
            (let [v (aget ea "uuid")]
              {:db (assoc db :Leito.db.log/selected-item-key v)})))