(ns ged.settings-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::settings
 (fn [db _]
   (:ged.db.settings/settings db)))



(rf/reg-sub
 ::wms-use-auth?
 (fn [db _]
   (:ged.db.settings/wms-use-auth? db)))

(rf/reg-sub
 ::geometry-name
 (fn [db _]
   (:ged.db.settings/geometry-name db)))