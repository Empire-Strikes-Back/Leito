(ns Leito.auth-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::auth
 (fn [db _]
   (:Leito.db.auth/auth db)))



