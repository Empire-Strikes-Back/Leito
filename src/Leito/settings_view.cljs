(ns Leito.settings-view
  (:require  [reagent.core :as r]
             [re-frame.core :as rf]
             [cljs.repl :as repl]
             [cljs.pprint :as pp]
             [Leito.settings-evs :as evs]
             [Leito.settings-subs :as subs]
             [Leito.settings-core]
             ["antd/lib/row" :default AntRow]
             ["antd/lib/col" :default AntCol]
             ["antd/lib/select" :default AntSelect]
             ["antd/lib/input" :default AntInput]
             ["antd/lib/button" :default AntButton]
             ["antd/lib/checkbox" :default AntCheckbox]

             #_["antd/lib/button" :default ant-Button]
             #_["antd/lib/table" :default AntTable]))


(def ant-row (r/adapt-react-class AntRow ))
(def ant-col (r/adapt-react-class AntCol))
(def ant-select (r/adapt-react-class AntSelect))
(def ant-select-option (r/adapt-react-class (.-Option AntSelect)))
(def ant-input (r/adapt-react-class AntInput))
(def ant-button (r/adapt-react-class AntButton))
(def ant-checkbox (r/adapt-react-class AntCheckbox))



(defn panel []
  (let [ awms-use-auth? (rf/subscribe [::subs/wms-use-auth?])
        ageometry-name (rf/subscribe [::subs/geometry-name])
        ]
    (fn []
      (let [wms-use-auth? @awms-use-auth?
            geometry-name @ageometry-name]
        [:section
         #_[:div "settings"]
         #_[:br]
         #_[ant-row
            [ant-col {:span 3} "world: "]
            [ant-col {:span 4}
             [ant-select {:default-value "hello"
                          :style {:width "120px"}
                          :on-change (fn [v] (js/console.log v))}
              [ant-select-option {:value "hello"} "hello"]
              [ant-select-option {:value "hi"} "hi"]]]]
         #_[ant-row
            [ant-col {:span 3} "proxy path"]
            [ant-col {:span 8}
             [ant-input {:value @proxy-path
                         :on-change
                         #(rf/dispatch [:Leito.settings.events/set
                                        :Leito.db.core/proxy-path
                                        (.. % -target -value)])}]]
            [ant-col {:span 4}
             [ant-button
              {:on-click (fn [] (rf/dispatch [:Leito.evs/apply-server-settings]))}
              "apply"]]]
         [:br]
         [ant-row
          [ant-col {:span 4} "use auth with /wms requests"]
          [ant-col {:span 8}
           [ant-checkbox {:size "small"
                          :checked wms-use-auth?
                          :on-change
                          #(rf/dispatch [::evs/set
                                         :Leito.db.settings/wms-use-auth?
                                         (.. % -target -checked)])}]]]
         [:br]
         [ant-row
          [ant-col {:span 4} "geometry property name"]
          [ant-col {:span 8}
           [ant-input {:size "small"
                       :value geometry-name
                       :on-change
                       #(rf/dispatch [::evs/set
                                      :Leito.db.settings/geometry-name
                                      (.. % -target -value)])}]]]
         [:br]
         ;
         ]
        ;
        ))))

(defn module-actions []
  [])
