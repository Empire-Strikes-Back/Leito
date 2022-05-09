(ns Leito.David.rest-view
  (:require  [reagent.core :as r]
             [re-frame.core :as rf]
             [cljs.repl :as repl]
             [cljs.pprint :as pp]
             [Leito.David.rest-evs :as evs]
             [Leito.David.rest-subs :as subs]
             [Leito.David.rest-core]

             ["react-ace/lib/index.js" :default ReactAce]
             ["brace" :as brace]
             ["brace/mode/clojure.js"]
             ["brace/mode/graphqlschema.js"]
             ["brace/mode/json.js"]
             ["brace/mode/xml.js"]
             ["brace/theme/github.js"]

             ["antd/lib/row" :default AntRow]
             ["antd/lib/col" :default AntCol]
             ["antd/lib/icon" :default AntIcon]
             ["antd/lib/button" :default AntButton]
             ["antd/lib/button/button-group" :default AntButtonGroup]
             ["antd/lib/input" :default AntInput]
             ["antd/lib/select" :default AntSelect]
             ["antd/lib/progress" :default AntProgress]
             ["antd/lib/input/Search" :default AntInputSearch]
             ["antd/lib/table" :default AntTable]
             ["antd/lib/popover" :default AntPopover]
             ["antd/lib/popconfirm" :default AntPopconfirm]
             
             
             ["antd/lib/auto-complete" :default AntAutoComplete]))


(def react-ace (r/adapt-react-class ReactAce))

(def ant-icon (r/adapt-react-class AntIcon))
(def ant-button (r/adapt-react-class AntButton))
(def ant-button-group (r/adapt-react-class AntButtonGroup))
(def ant-input (r/adapt-react-class AntInput))
(def ant-select (r/adapt-react-class AntSelect))
(def ant-select-option (r/adapt-react-class (.-Option AntSelect)))
(def ant-row (r/adapt-react-class AntRow))
(def ant-col (r/adapt-react-class AntCol))

(def ant-input-search (r/adapt-react-class AntInputSearch))
(def ant-auto-complete (r/adapt-react-class AntAutoComplete))
(def ant-auto-complete-option (r/adapt-react-class (.-Option AntAutoComplete)))
(def ant-table (r/adapt-react-class AntTable))
(def ant-popover (r/adapt-react-class AntPopover))
(def ant-popconfirm (r/adapt-react-class AntPopconfirm))



(defn editor-data
  []
  (let [default-value ""
        av (r/atom default-value)]
    (fn []
      [react-ace {:name "rest-editor-data"
                  :mode "json"
                  :theme "github"
                  :className "rest-editor-data"
                  :width "80vw"
                  :height "80vh"
                    ;  :default-value default-value
                  :value @av
                  :on-load (fn [ref]
                             (rf/dispatch [:Leito.David.rest-core/set-editor-preserve [:data ref] ])
                             #_(do (core/set-editor-preserve! :data ref)))
                  :on-change (fn [v ev] (reset! av v))
                  :editor-props {"$blockScrolling" js/Infinity}}])))

(defn editor-response
  []
  (let [default-value ""
        av (r/atom default-value)]
    (fn []
      [react-ace {:name "rest-editor-response"
                  :mode "xml"
                  :theme "github"
                  :className "rest-editor-response"
                  :width "32vw"
                    ;  :default-value default-value
                  :value @av
                  :on-load (fn [ref]
                             (rf/dispatch [:Leito.David.rest-core/set-editor-preserve [:response ref]])
                             #_(do (core/set-editor-preserve! :response ref)))
                  :on-change (fn [v ev] (reset! av v))
                  :editor-props {"$blockScrolling" js/Infinity}}])))

(defn select-endpoint
  []
  (let [selected-url (rf/subscribe [::subs/selected-url])
        on-select (fn [ev]
                    (js/console.log ev)
                    (rf/dispatch [::evs/selected-url ev]))]
    (fn []
      [ant-select {:value @selected-url
                   :on-select on-select
                   :style {:width "100%"}}
       [ant-select-option
        {:value "/rest/workspaces/dev/featuretypes.json"}
        "/rest/workspaces/dev/featuretypes.json"]
       [ant-select-option
        {:value "/rest/workspaces/dev/datastores/pgdb/featuretypes.json"}
        "/rest/workspaces/dev/datastores/pgdb/featuretypes.json"]
       ])))



(defn btn-fetch
  []
  [ant-button
   {:icon "reload"
    :on-click #(rf/dispatch [::evs/fetch-selected-url])}])

(defn layer-id-input
  []
  (let [av (rf/subscribe [::subs/layer-id-input])]
    (fn []
      (let [v @av]
        [ant-input {:value v
                    :on-change (fn [ev]
                                 (rf/dispatch [::evs/layer-id-input
                                               (.. ev -target -value)]))
                    :placeholder "topp:states"
                    :size "small"}]))))

(defn fetch-layer-button
  []
  [ant-button
   {:icon "reload"
    :size "small"
    :on-click #(rf/dispatch [::evs/fetch-layer])}])

(def base-columns
  [{:title "name"
    :key "name"
    :dataIndex "name"}])

(def extra-columns
  [{:title "action"
    :key "action"
    :width "64px"
    :render (fn [txt rec idx]
              (r/as-element
               [ant-button-group
                {:size "small"}
                [ant-button
                 {;:icon "plus"
                  :type "primary"

                  :on-click #(rf/dispatch
                              [::evs/select-feature
                               rec])}
                 "select"]]))}
   #_{:title ""
      :key "empty"}])

(def columns (vec (concat base-columns extra-columns)))

(def row-key :name)

(defn table
  []
  (let [lst (rf/subscribe [::subs/fetch-selected-url-list])
        table-mdata (rf/subscribe [::subs/search-table-mdata])]
    (fn []
      (let [items @lst
            total (count items)
            ents items
            #_(mapv #(-> % :entity (dissoc :db/id)) items)
            pagination (:pagination @table-mdata)]
        [ant-table {:show-header true
                    :size "small"
                    :row-key row-key
                    :className "rest-table"
                    :columns columns
                    :dataSource ents
                    :on-change (fn [pag fil sor ext]
                                 (rf/dispatch [::evs/search-table-mdata
                                               (js->clj {:pagination pag
                                                         :filters fil
                                                         :sorter sor
                                                         :extra ext} :keywordize-keys true)]))
                    :scroll {;  :x "max-content" 
                                ;  :y 256
                             }
                        ; :rowSelection {:on-change (fn [keys rows]
                        ;                             (prn keys)
                        ;                             )}
                    :pagination (merge pagination
                                       {:total total
                                            ; :on-change #(js/console.log %1 %2)
                                        })}]))))



(defn panel
  []
  #_(js/console.log 'count-panel-fn)
  #_(rf/dispatch [::evs/nutrients])
  #_(rf/dispatch [::evs/nhi-dri])
  (let []
    (fn []
      [:section
       [:div
        #_[ant-row
         [ant-col {:span 10} [select-endpoint]]
         [ant-col {:span 2} [btn-fetch]]]]
       [ant-row 
        [ant-col {:span 10} [layer-id-input]]
        [ant-col {:span 2} [fetch-layer-button]]
        [ant-col {:span 1}
         [ant-popover
          {:content
           (r/as-element
            [:div
             [:span "This page is for editing featuretypes (layers) via REST."]
             [:br]
             [:span "Docs:"]
             [:br]
             [:a {:target "_blank"
                  :href "https://docs.geoserver.org/latest/en/api/#/latest/en/api/1.0.0/featuretypes.yaml"}
              "geoserver rest featuretypes"]])}
          [ant-button
           {:icon "question"
            :shape "circle"
            :size "small"
            :style {:margin-left "4px"
                    :width "12px" :height "12px"
                    :font-size "8px" :min-width "initial"}}]]
         ]
        ]
       [:br]
       #_[table]
       [:br]
       [:section {:class "editors-container"}
        [editor-data]
        #_[editor-response]]
       [:br]
       [ant-button-group {:size "small"}
        [ant-button {:on-click
                     #(rf/dispatch [::evs/tx-item-2 {:tx-type :post}])
                     :style {:width "96px"}}
         "post"]
        [ant-button {:on-click
                     #(rf/dispatch [::evs/tx-item-2 {:tx-type :put}])
                     :style {:width "96px"}}
         "put"]
        
        [ant-popconfirm
         {:title "delete layer?" 
          :on-confirm #(rf/dispatch [::evs/tx-item-2 {:tx-type :delete}])
          :okText "yes" :cancelText "no"}
         [ant-button {:ghost true
                      :type "danger"
                      :style {:width "96px"}}
          "delete"]
         ]
        ]
       [:br]


       #_[Leito.David.rest-sample/sample-table]])))

