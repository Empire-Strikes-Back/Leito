(ns Leito.home-subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::module-count
 (fn [db _]
   (:Leito.db.core/module-count db)))