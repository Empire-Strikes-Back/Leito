(ns ged.feats-view
  (:require  [reagent.core :as r]
             [cljs.repl :as repl]
             [cljs.pprint :as pp]
             [re-frame.core :as rf]
             [ged.feats-subs :as subs]
             [ged.feats-evs :as evs]
             [ged.feats-core]

             ["react-ace/lib/index.js" :default ReactAce]
             ["brace" :as brace]
             ["brace/mode/clojure.js"]
             ["brace/mode/graphqlschema.js"]
             ["brace/mode/json.js"]
             ["brace/mode/xml.js"]
             ["brace/mode/sql.js"]
             ["brace/theme/github.js"]

             ["antd/lib/icon" :default AntIcon]
             ["antd/lib/button" :default AntButton]
             ["antd/lib/button/button-group" :default AntButtonGroup]
             ["antd/lib/input" :default AntInput]
             ["antd/lib/progress" :default AntProgress]
             ["antd/lib/input/Search" :default AntInputSearch]
             ["antd/lib/table" :default AntTable]
             ["antd/lib/auto-complete" :default AntAutoComplete]
             ["antd/lib/row" :default AntRow]
             ["antd/lib/col" :default AntCol]
             ["antd/lib/checkbox" :default AntCheckbox]
             ["antd/lib/popover" :default AntPopover]
             ["antd/lib/popconfirm" :default AntPopconfirm]
             
             
             #_[ged.core.extra :refer [extra-component]]))



(def ant-icon (r/adapt-react-class AntIcon))
(def ant-button (r/adapt-react-class AntButton))
(def ant-button-group (r/adapt-react-class AntButtonGroup))
(def ant-input (r/adapt-react-class AntInput))
(def ant-input-search (r/adapt-react-class AntInputSearch))
(def ant-auto-complete (r/adapt-react-class AntAutoComplete))
(def ant-auto-complete-option (r/adapt-react-class (.-Option AntAutoComplete)))
(def ant-table (r/adapt-react-class AntTable))
(def ant-row (r/adapt-react-class AntRow))
(def ant-col (r/adapt-react-class AntCol))
(def ant-checkbox (r/adapt-react-class AntCheckbox))
(def ant-popover (r/adapt-react-class AntPopover))
(def ant-popconfirm (r/adapt-react-class AntPopconfirm))


(def react-ace (r/adapt-react-class ReactAce))

(defn editor-feature
  []
  (let [default-value ""
        av (r/atom default-value)]
    (fn []
      [react-ace {:name "editor-feature"
                  :mode "json"
                  :theme "github"
                  :className "editor-feature"
                  :width "100%"
                    ;  :default-value default-value
                  :value @av
                  :on-load (fn [ref]
                             (rf/dispatch [:ged.feats-core/set-editor-preserve [:data ref]]))
                  :on-change (fn [v ev] (reset! av v))
                  :editor-props {"$blockScrolling" js/Infinity}}])))

(defn editor-response
  []
  (let [default-value ""
        av (r/atom default-value)]
    (fn []
      [react-ace {:name "editor-response"
                  :mode "xml"
                  :theme "github"
                  :className "editor-response"
                  :width "32vw"
                    ;  :default-value default-value
                  :value @av
                  :on-load (fn [ref]
                             (rf/dispatch [:ged.feats-core/set-editor-preserve [:response ref]]))
                  :on-change (fn [v ev] (reset! av v))
                  :editor-props {"$blockScrolling" js/Infinity}}])))

(defn editor-request
  []
  (let [default-value ""
        av (r/atom default-value)]
    (fn []
      [react-ace {:name "editor-request"
                  :mode "xml"
                  :theme "github"
                  :className "editor-request"
                  :width "32vw"
                    ;  :default-value default-value
                  :value @av
                  :on-load (fn [ref]
                             (rf/dispatch [:ged.feats-core/set-editor-preserve [:request ref]]))
                  :on-change (fn [v ev] (reset! av v))
                  :editor-props {"$blockScrolling" js/Infinity}}])))


(defn editor-ecql
  []
  (let [default-value
        "CITY_NAME ilike '%@input%' OR STATUS ilike '%national%capital%'"
        av (r/atom default-value)]
    (fn []
      [react-ace {:name "editor-ecql"
                  :mode "sql"
                  :theme "github"
                  :className "editor-ecql"
                  :width "100%"
                  :disabled true
                    ;  :default-value default-value
                  :value @av
                  :on-load (fn [ref]
                             (rf/dispatch [:ged.feats-core/set-editor-preserve [:ecql ref]]))
                  :on-change (fn [v ev] (reset! av v))
                  :editor-props {"$blockScrolling" js/Infinity}}])))

(defn auto-complete-suffix
  [{:keys [on-click]}]
  [ant-button
   {:class "search-btn"
    :style {:margin-right "-12px"}
    :size "small"
    :on-click on-click
    :type "default"}
   [ant-icon {:type "search"}]])

(defn auto-complete
  [{:keys []}]
  (let [state (r/atom {:input ""})
        on-select (fn [s]
                    (prn "selected " s))
        on-search (fn [s]
                    (rf/dispatch [::evs/search {:input s}]))
        on-change (fn [s]
                    #_(prn "s:" (.. evt -target -value))
                    (swap! state assoc :input s))
        on-key-up (fn [evt]
                    (when (= (.-key evt) "Enter")
                      (on-search (.. evt -target -value))))]
    (fn [_]
      [ant-auto-complete
       {
        :style {:width "100%"}
        :size "small"
        :placeholder "search text attributes (select below)"
        :on-search on-change
        :on-select on-select
        :option-label-prop "text"}
       [ant-input
        {:value (:input @state)
         :on-press-enter on-key-up
        ;  :on-key-up on-key-up
         :suffix (r/as-element [auto-complete-suffix
                                {:on-click #(on-search (:input @state))}])}]])))

(defn feature-type-input
  []
  (let [sref (rf/subscribe
               [::subs/feature-type-input])]
    (fn []
      [ant-input {:size "small"
                  :style {:width "calc(100% - 25px)"  :margin "0 0 0 0px"}
                  :value @sref
                  :on-change
                  (fn [ev]
                    (rf/dispatch
                     [::evs/feature-type-input
                      (.. ev -target -value)]))
                  :placeholder "topp:states"}])))

(defn fetch-ftype-mdata-button
  []
  (let [ ]
    (fn []
      [ant-button {:icon "reload"
                   :size "small"
                   :title "fetch layer data"
                   :on-click (fn []
                               (rf/dispatch [::evs/fetch-ftype-mdata]))}])))

(defn feature-ns
  []
  (let [afns (rf/subscribe
              [::subs/feature-ns])]
    (fn []
      [:div  (or @afns "-")]
      #_[ant-input {:size "small"
                  :value @sref
                  :on-change
                  (fn [ev]
                    (rf/dispatch
                     [::evs/feature-ns
                      (.. ev -target -value)]))
                  :placeholder "http://www.opengis.net/wfs/dev"}])))

(def feature-columns
  [{:title "id"
    :key "id"
    :dataIndex "id"}
   {:title "preview"
    :key "preview"
    :render 
    (fn [txt rec idx]
      (let [v (js/JSON.stringify (aget rec "properties"))]
        (r/as-element
         [:div {:title v
                :style  {:white-space "nowrap"
                         :max-width "216px"
                         :overflow-x "hidden"}}
          v])
        )
      )
    }])

(def extra-columns
  [{:title "action"
    :key "action"
    :width "48px"
    :render (fn [txt rec idx]
              (r/as-element
               [ant-button-group
                {:size "small"}
                [ant-button
                 {;:icon "plus"
                  :type "primary"

                  :on-click #(rf/dispatch
                              [::evs/select-feature
                               rec])
                  }
                 "select"]]))}
   #_{:title ""
      :key "empty"}])

(def columns (vec (concat feature-columns extra-columns)))

(defn table
  []
  (let [search-res (rf/subscribe [::subs/search-res])
        table-mdata (rf/subscribe [::subs/search-table-mdata])]
    (fn []
      (let [items (:features @search-res)
            total (:totalFeatures @search-res)
            ents items
            #_(mapv #(-> % :entity (dissoc :db/id)) items)
            pagination (:pagination @table-mdata)]
        [ant-table {:show-header true
                    :size "small"
                    :row-key :id
                    :style {:height "50%" :width "100%"}
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
                                        :showTotal (fn [t rng] t)
                                            ; :on-change #(js/console.log %1 %2)
                                        })}]))))

(def table-attrs-columns
  [{:title "attribute"
    :key :name
    :dataIndex :name
    }
   {:title "type"
    :key :type
    :dataIndex :binding}
   ]
  )

(defn table-attrs
  []
  (let [adata (rf/subscribe [::subs/layer-attributes])
        aselected (rf/subscribe [::subs/selected-attrs])]
    (fn []
      (let [data @adata
            selected @aselected  ]
        [ant-table {:show-header true
                    :size "small"
                    :row-key :name
                    :style {:height "30%" :overflow-y "auto"}
                    :columns table-attrs-columns
                    :dataSource data
                    :scroll {:y 216}
                    :pagination false
                    :rowSelection {:selectedRowKeys selected
                                   :on-change
                                   (fn [ks rows ea]
                                     (rf/dispatch [::evs/selected-attrs ks]))}}]))))

(defn use-eqcl-checkbox
  []
  (let []
    (fn []
      [ant-row
       [ant-col {:span 2}
        [ant-checkbox
         {:on-change (fn [ev] (rf/dispatch
                               [::evs/use-eqcl-filter?
                                (.. ev -target -checked)]))}]]
       [ant-col {:span 10} 
        [:div 
         [:span "use ecql filter instead"]
         [:span 
          [ant-popover
           {:content
            (r/as-element
             [:div
              [:span "docs:"]
              [:br]
              [:a {:target "_blank"
                   :href "https://docs.geoserver.org/stable/en/user/filter/ecql_reference.html#filter-ecql-reference"}
               "eqcl reference"]
              [:br]
              [:a {:target "_blank"
                   :href "https://docs.geoserver.org/stable/en/user/tutorials/cql/cql_tutorial.html#cql-tutorial"}
               "cql-tutorial"]]
             )}
           [ant-button
            {:icon "question"
             :shape "circle"
             :size "small"
             :style {:margin-left "4px"
                     :width "12px" :height "12px"
                     :font-size "8px" :min-width "initial"}}]
           ]
          ]
         ]
        [:small "use @input variable (will be replaced with the input)"]
        ]
       ])))

(defn panel
  []
  #_(js/console.log 'count-panel-fn)
  #_(rf/dispatch [::evs/nutrients])
  #_(rf/dispatch [::evs/nhi-dri])
  (let []
    (fn []
      [:div {:style {:height "100%" :width "100%" :display "flex" }}
       [:section {:style {:width "47%"}}
        [:span
         [feature-type-input]
         [fetch-ftype-mdata-button]]
        [:br] [:br]
        [feature-ns]
        [:br]
        [auto-complete {}]
        [:br] [:br]
        [table-attrs]
        [:br]
        [use-eqcl-checkbox]
        [:br]
        [editor-ecql]
        ]
       [:section {:style {:width "4%" }}]
       [:section {:style {:width "47%"}}
        [table]
        [:br]
        [editor-feature]
        [ant-button-group {:size "small"}
         [ant-button {:on-click
                      #(rf/dispatch [::evs/tx-feature {:tx-type :inserts}])
                      :style {:width "96px"}}
          "insert"]
         [ant-button {:on-click
                      #(rf/dispatch [::evs/tx-feature {:tx-type :updates}])
                      :style {:width "96px"}}
          "update"]
         [ant-popconfirm
          {:title "deelte feature?"
           :on-confirm #(rf/dispatch [::evs/tx-feature {:tx-type :deletes}])
           :okText "yes" :cancelText "no"}
          [ant-button {:ghost true
                       :type "danger"
                       :style {:width "96px"}}
           "delete"]]
         ]]]
      )))

