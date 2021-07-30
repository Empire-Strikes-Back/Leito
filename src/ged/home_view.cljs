(ns ged.home-view
  (:require  [reagent.core :as r]
             [cljs.repl :as repl]
             [cljs.pprint :as pp]
             [re-frame.core :as rf]
             [ged.home-subs :as subs]
             [ged.home-evs :as evs]
             ["antd/lib/button" :default ant-Button]
             #_[ged.core.extra :refer [extra-component]]))

(defonce cnt (atom 0))

(def ant-button (r/adapt-react-class ant-Button))

(comment
  
  (swap! cnt inc)
  ;
  )


(defn my-component
  [x y z]
  (let [some (r/atom {})      ;; <-- closed over by lifecycle fns
        can  (fn [])]
    (r/create-class                 ;; <-- expects a map of functions 
     {:display-name  "my-component"      ;; for more helpful warnings & errors

      :component-did-mount               ;; the name of a lifecycle function
      (fn [this]
        (println "component-did-mount")) ;; your implementation

      :component-did-update              ;; the name of a lifecycle function
      (fn [this old-argv]                ;; reagent provides you the entire "argv", not just the "props"
        (let [new-argv (rest (r/argv this))]
          (js/console.log new-argv old-argv)))

        ;; other lifecycle funcs can go in here


      :reagent-render        ;; Note:  is not :render
      (fn [x y z]           ;; remember to repeat parameters
        [:div (str x " " y " " z)])})))

(defn my-div []
  (let [this (r/current-component)]
    (into [:div.custom (r/props this)]
          (r/children this))))

(defn stateful-comp
  []
  (let [component-state (r/atom {:count 0})]
    (fn []
      [:div ;; That returns hiccup
       [:p "count is: " (get @component-state :count)]
       [:button {:on-click #(swap! component-state update-in [:count] inc)} "Increment"]])))

(defn panel []
  (let [module-count @(rf/subscribe [::subs/module-count])
        base-url @(rf/subscribe [:ged.subs/base-url] )
        ]
    [:div
     [:p (str "I am a component!! " @cnt)]
     [:p (str "loaded modules count: " module-count) ]
     [:base-url (str "base url is: " base-url) ]
     [:p.someclass
      "I have " [:strong "bold"]
      [:span {:style {:color "red"}} " and red "] "text."]
     [ant-button {:on-click (fn [] (rf/dispatch [::evs/inc-module-count]) )
                  } "inc module count"]
     #_[extra-component]
     [stateful-comp]
     [:div
      (map (fn [x]
             [:div {:key x :id x} x]
             ) (range 1 10) )
      ]
     ]))

