(ns ged.routes
  (:require [clojure.repl]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            #_[clerk.core :as clerk]
            #_[goog.events ]
            [reagent.core :as r]
            [re-frame.core :as rf])
  (:import goog.History
           goog.history.Html5History
           goog.history.Html5History.TokenTransformer
           goog.history.EventType
           goog.Uri))

(def routes ["/" {""      :map
                  "home" :home
                  "settings" :settings
                  "map" :map
                  "feats" :feats
                  "rest" :rest
                  "auth" :auth
                  "log" :log}])

#_(def _ (events/listen history EventType.NAVIGATE
                      (fn [e]
                        (when-let [match (-> (.-token e) match-fn identity-fn)]
                          (dispatch-fn match)))))

#_(defn ^:export clerk-nav
  [url]
  (clerk/navigate-page! url)
  )

#_(defn ^:export clerk-render
  []
  (r/after-render clerk/after-render!))

(defn- parse-url [url]
  (merge
   {:url url}
   (bidi/match-route routes url)))

(defn- dispatch-route [matched-route]
  (let [handler (:handler matched-route)
        panel-name (if handler
                     (keyword (str (name handler) "-panel"))
                     (:url matched-route))
        url (:url matched-route)]
    #_(prn "matched-route: " matched-route)
    #_(r/after-render clerk/after-render!)
    (rf/dispatch [:ged.evs/set-active-panel panel-name])
    #_(clerk/navigate-page! url)))

(declare history)

(defn app-routes []
  #_(clerk/initialize!)
  (defonce history (pushy/pushy dispatch-route parse-url))
  (pushy/start! history))

#_(defn app-routes []
  #_(clerk/initialize!)
  (pushy/start! (pushy/pushy dispatch-route parse-url))
  )

;Cannot infer target type in expression
;https://clojurescript.org/guides/externs#externs-inference
(defn ^:export set-path!
  [path]
  #_(set-token! history path)
  (.setToken ^js/Object (.-history history)  path))

#_(defn get-path
  []
  (get-token history))



(def path-for (partial bidi/path-for routes))

