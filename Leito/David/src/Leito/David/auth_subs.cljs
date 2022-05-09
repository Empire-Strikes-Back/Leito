(ns Leito.David.auth-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::auth
 (fn [db _]
   (:Leito.David.db.auth/auth db)))



