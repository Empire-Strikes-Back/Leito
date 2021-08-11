(ns ged.layout
  (:require [cljs.repl :as repl]
            [cljs.pprint :as pp]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [clojure.string]
            [ged.routes]
            ["antd/lib/layout" :default AntLayout]
            ["antd/lib/layout/Sider" :default AntSider]
            ["antd/lib/menu" :default AntMenu]
            ["antd/lib/icon" :default AntIcon]
            #_["antd/lib/menu" :default AntMenu]
            #_["antd/lib/icon" :default AntIcon]))

(def ant-layout (r/adapt-react-class AntLayout))
(def ant-layout-content (r/adapt-react-class (.-Content AntLayout)))
(def ant-layout-header (r/adapt-react-class (.-Header AntLayout)))

(def ant-sider (r/adapt-react-class AntSider))
#_(def ant-menu (r/adapt-react-class AntMenu))
#_(def ant-menu-item (r/adapt-react-class (.-Item AntMenu)))
#_(def ant-icon (r/adapt-react-class AntIcon))


(def ant-menu (r/adapt-react-class AntMenu))
(def ant-menu-item (r/adapt-react-class (.-Item AntMenu)))
(def ant-icon (r/adapt-react-class AntIcon))

(defn menu
  [{:keys [on-select]}]
  (let [active-panel (rf/subscribe [:ged.subs/active-panel])]
    (fn []
      [ant-menu {:theme "light"
                 :mode "horizontal"
                 :size "small"
                 :style {:lineHeight "32px"}
                 :default-selected-keys ["home-panel"]
                 :selected-keys (if @active-panel [(name @active-panel)] nil)
                 :on-select on-select}
       #_[ant-menu-item {:key "home-panel"}
          [ant-icon {:type "home"}]
          [:span "home"]]

       [ant-menu-item {:key "map-panel"}
        [ant-icon {:type "global"}] #_"compas" #_"environment" #_"global"
        [:span "map"]]
       [ant-menu-item {:key "feats-panel"}
        [ant-icon {:type "table"}] #_"compas" #_"environment" #_"global"
        [:span "feats"]]
       [ant-menu-item {:key "rest-panel"}
        [ant-icon {:type "swap"}]
        [:span "rest"]]
       [ant-menu-item {:key "settings-panel"}
        [ant-icon {:type "setting"}]
        [:span "settings"]]
       [ant-menu-item {:key "auth-panel"}
        [ant-icon {:type "user"}]
        [:span "auth"]]
       [ant-menu-item {:key "log-panel"}
        [ant-icon {:type "history"}]
        [:span "log"]]
       #_[ant-menu-item {:key "monitor-panel"}
          [ant-icon {:type "monitor"}]
          [:span "monitor"]]])))


(defn layout
  [menu content]
  [ant-layout {:style {:min-height "100vh"
                       }}
   #_[ant-sider {:collapsible true :theme "light" :default-collapsed true}
    [:div {:class "logo"}
     [:img {:class "logo-img" :src "./img/logo-4.png"}]
     [:div {:class "logo-name"} "ged"]
     ]
    menu]
   [ant-layout-header
    {:style {:position "fixed"
             :z-index 1
             :lineHeight "32px"
             :height "32px"
             :padding 0
             :background "#000" #_"#001529"
             :width "100%"}}
    [:a {:href "/"
         :class "logo"}
     [:img {:class "logo-img" :src "./img/logo-4.png"}]
     [:div {:class "logo-name"} "ged"]]
    menu]
   [ant-layout-content {:class "main-content"
                        :style {:margin-top "32px"
                                :padding "32px 32px 32px 32px"}}
    content]
   ])

