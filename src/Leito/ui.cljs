(ns Leito.ui
  (:require [cljs.repl :as repl]
            [cljs.pprint :as pp]
            [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [re-pressed.core :as rp]
            [Leito.evs :as evs]
            [Leito.routes :as routes]
            [Leito.config :as config]
            [Leito.subs :as subs]
            [Leito.view]
            #_[tools.core]
            [Leito.core]
            [Leito.req]
            [Leito.async]
            [devtools.core :as devtools]
            ;dev
            [Leito.notepad]))

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
  (r/render [Leito.view/ui]
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