(ns Leito.David.main
  (:require [cljs.repl :as repl]
            [cljs.pprint :as pp]
            [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [re-pressed.core :as rp]
            [Leito.David.evs :as evs]
            [Leito.David.routes :as routes]
            [Leito.David.config :as config]
            [Leito.David.subs :as subs]
            [Leito.David.view]
            #_[tools.core]
            [Leito.David.core]
            [Leito.David.req]
            [Leito.David.async]
            [devtools.core :as devtools]
            ;dev
            [Leito.David.notepad]))

(devtools/install!)
#_(enable-console-print!)

#_(prn "3")
#_(println "3")


#_(sample.lib.logic/example)

#_(repl/dir rf)

#_(re-frame.loggers/get-loggers)

(defn dev-setup []
  (when config/debug?
    (println "dev mode" config/debug?)))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [Leito.David.view/ui]
            (.getElementById js/document "ui")))

(defn ^:export main []
  (routes/app-routes)
  (rf/dispatch-sync [::evs/initialize-db])
  (rf/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  #_(rf/dispatch-sync [::evs/apply-server-settings])
  (dev-setup)
  (mount-root))

(defn ^:dev/after-load after-load []
  #_(js/console.log "--after load")
  (mount-root))

; (defonce _ (main))

(def clog (.-log js/console))

(comment

  (ns sniff.core)

  (repl/dir cljs.repl)

  (clog 3)

  (->
   (js/fetch "http://localhost:7881/attrs")
   (.then (fn [r]
            (.json r)))
   (.then (fn [r]
            ; (clog r)
            (prn r))))

  (pp/pprint 3)

  ;
  )