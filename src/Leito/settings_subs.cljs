(ns Leito.settings-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::settings
 (fn [db _]
   (:Leito.db.settings/settings db)))



(rf/reg-sub
 ::wms-use-auth?
 (fn [db _]
   (:Leito.db.settings/wms-use-auth? db)))

(rf/reg-sub
 ::geometry-name
 (fn [db _]
   (:Leito.db.settings/geometry-name db)))