(ns ged.settings-evs
  (:require [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
            [goog.dom]
            [ged.settings-core]))


(rf/reg-event-fx
 ::set
 (fn-traced [{:keys [db]} [_ key v]]
            (let []
              {:db (assoc db key v)
               :dispatch [:assoc-in-store [[key] v]]})))