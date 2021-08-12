(ns ged.subs
  (:require [re-frame.core :as rf])
  )

(rf/reg-sub
 ::active-panel
 (fn [db _]
   (:ged.db.core/active-panel db)))

(rf/reg-sub
 ::module-count
 (fn [db _]
   (:ged.db.core/module-count db)))

(rf/reg-sub
 ::api
 (fn [db _]
   (get-in db [:ged.db.core/api])))


#_(fn [_ _]  [(rf/subscribe [:active-attribute]) (rf/subscribe [:entities-table-state])] )
#_(fn [[active-attribute entities-table-state] _])

(rf/reg-sub
 ::base-url
 (fn [_ _] [ (rf/subscribe [::api])])
 (fn [[ api] _]
   (get-in api [:base-url])))



(rf/reg-sub
 ::profiles
 (fn [db _]
   (:ged.db.core/profiles db)))

(rf/reg-sub
 ::active-profile
 (fn [db _]
   (->>
    (:ged.db.core/profiles db)
    (vals)
    (filterv (fn [pf]
               (:active? pf)))
    (first))))

(rf/reg-sub
 ::active-profile-key
 (fn [db _]
   (:ged.db.core/active-profile-key db)))

(rf/reg-sub
 ::geoserver-host
 (fn [db _]
   (let [apk (:ged.db.core/active-profile-key db)]
     (get-in db [:ged.db.core/profiles apk :host]))))

(rf/reg-sub
 ::credentials
 (fn [db _]
   (let [apk (:ged.db.core/active-profile-key db)
         username (get-in db [:ged.db.core/profiles apk :username])
         password (get-in db [:ged.db.core/profiles apk :password])]
     {:password password
      :username username})))

