(ns ged.storage
  (:require [clojure.repl :as repl]
            [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
            [ged.core :refer [deep-merge]]
            [cljs.reader]
            [ged.db]))

(defn ls-key
  []
  "ged-db")

(defn ^:export read-db
  []
  (some->>
   (.getItem js/localStorage (ls-key))
   (cljs.reader/read-string)))

(defn write-db!
  [v]
  (.setItem js/localStorage (ls-key) (str v)))

(defn ^:export remove-db!
  []
  (.removeItem js/localStorage (ls-key)))

(defn assoc-in-store!
  [path v]
  (let [db (read-db)
        nx (assoc-in db path v)]
    (write-db! nx)))

(defn deep-merge-store!
  [& maps]
  (let [db (read-db)
        nx (apply (partial deep-merge db) maps)]
    (write-db! nx)))

(defn read-profile-db
  [k]
  (let [db (read-db)]
    (get-in db [:profile-dbs k])))

(defn read-profiles
  []
  (let [db (read-db)]
    (get-in db [:profiles])))

(defn read-active-profile-key
  [k]
  (let [db (read-db)]
    (get-in db [:active-profile-key])))

#_(deep-merge-store! {:x 3} {:y 4})
#_(read-db)
#_ (remove-db!)

#_(assoc-in-store! [:s] 3 )

; https://clojuredocs.org/clojure.core/some-%3E%3E

#_(repl/dir rf)

