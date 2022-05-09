(ns Leito.David.view
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require [cljs.repl :as repl]
            [cljs.pprint :as pp]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [shadow.loader :as loader]
            [Leito.David.subs :as subs]
            [Leito.David.home-view]
            [Leito.David.settings-view]
            [cljs.core.async :refer [<! timeout]]
            [Leito.David.config :as config]
            [clojure.string]
            [Leito.David.routes]
            [Leito.David.layout :as layout]
            ["antd/lib/result" :default AntResult]
            ["antd/lib/button" :default AntButton]))

(def ant-result (r/adapt-react-class AntResult))
(def ant-button (r/adapt-react-class AntButton))


(defn fn-to-call-on-load []
  (js/console.log "module loaded"))

(defn fn-to-call-on-error []
  (js/console.log "module load failed"))


; @(resolve 'clojure.repl/dir)  wrong , macro
; ((resolve 'clojure.core/first) [1 2]) works

(defn- resolve-module
  [module-name]
  (case module-name
    "home" {:panel [(resolve 'Leito.David.home-view/panel)]
            :actions nil}
    "settings" {:panel [(resolve 'Leito.David.settings-view/panel)]}
    "map" {:panel [(resolve 'Leito.David.map-view/panel)]}
    "feats" {:panel [(resolve 'Leito.David.feats-view/panel)]}
    "rest" {:panel [(resolve 'Leito.David.rest-view/panel)]}
    "auth" {:panel [(resolve 'Leito.David.auth-view/panel)]}
    "log" {:panel [(resolve 'Leito.David.log-view/panel)]}
    [:div (str "no panel for module: " module-name)]))

(defn module->panel
  [module-name]
  (->
   (resolve-module module-name)
   (:panel)))

(defn module->actions
  [module-name]
  (or (->
       (resolve-module module-name)
       (:actions)) (fn [] nil)))

(defn panel->module-name
  [panel-name]
  (if panel-name
    (-> (name panel-name) (clojure.string/split #"-") first)
    nil))

(defn panel-defered
  [module-name]
  (let [comp-state (r/atom {})]
    (fn [module-name]
      (let [panel (@comp-state module-name)]
        ; (prn module-name)
        ; (prn panel)
        (cond
          (loader/loaded? module-name) (module->panel module-name)
          panel [panel]
          :else
          (do
            (go
              (<! (timeout (if config/debug? 0 0)))
              (-> (loader/load module-name)
                  (.then
                   (fn []
                     (rf/dispatch [:Leito.David.evs/inc-module-count])
                     (swap! comp-state update-in [module-name] (fn [_] (module->panel module-name))))
                   (fn [] (js/console.log (str "module load failed: " module-name))))))
            #_[:div "nothing"]
            [:div "loading..."])
          ;
          )))))

(defn not-found-panel
  [path]
  [ant-result {:status "404"
               :title "404"
               :subTitle (str path " not found")
              ;  :extra (r/as-element
              ;          [ant-button {:type "default"}
              ;           [:a {:href "/"} "/"]])
               }]
  )

(defn- panels [panel-name]
  (case panel-name
    :home-panel [Leito.David.home-view/panel]
    :settings-panel [Leito.David.settings-view/panel]
    :map-panel [panel-defered "map"]
    :feats-panel [panel-defered "feats"]
    :auth-panel [panel-defered "auth"]
    :rest-panel [panel-defered "rest"]
    :log-panel [panel-defered "log"]
    nil [:div "loading..."]
    [not-found-panel panel-name]))


(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))

(defn act-panel-con
  []
  (let [active-panel (rf/subscribe [::subs/active-panel])
        module-count @(rf/subscribe [::subs/module-count]) ; triggers render
        module-name (panel->module-name @active-panel)
        actions-fn (module->actions module-name)
        ]
    #_(prn "act-panel-con: " @active-panel)
    #_(prn "act-panel-con: " @module-count)
    #_(prn (actions-fn))
    #_[act-panel {:module-actions  (actions-fn) }]
    )
  )
; (keyword (str (name (:handler matched-route)) "-panel"))



(defn ui
  []
  [layout/layout
   [layout/menu
    {:on-select (fn [eargs]
                  (let [eargs-clj (js->clj eargs :keywordize-keys true)
                        {:keys [key]} eargs-clj]
                    (Leito.David.routes/set-path! (str "/" (panel->module-name (keyword key))))
                    #_(rf/dispatch [:Leito.David.evs/set-active-panel (keyword key)])))}]
   [main-panel]
   ]
  #_[main-panel])