(ns ged.map-view
  (:require  [reagent.core :as r]
             [cljs.repl :as repl]
             [cljs.pprint :as pp]
             [re-frame.core :as rf]
             [ged.map-subs :as subs]
             [ged.map-evs :as evs]
             [ged.map-core]
             [ged.core :refer [->clj  pretty-json
                               pretty-json-str]]
             ["react-ace/lib/index.js" :default ReactAce]
             ["brace" :as brace]
             ["brace/mode/json.js"]
             ["brace/theme/github.js"]

             ["antd/lib/button" :default AntButton]
             ["antd/lib/button/button-group" :default AntButtonGroup]
             ["antd/lib/radio" :default AntRadio]
             ["antd/lib/icon" :default AntIcon]
             ["antd/lib/row" :default AntRow]
             ["antd/lib/col" :default AntCol]
             ["antd/lib/table" :default AntTable]
             ["antd/lib/dropdown" :default AntDropdown]
             ["antd/lib/menu" :default AntMenu]
             ["antd/lib/input" :default AntInput]
             ["antd/lib/popconfirm" :default AntPopconfirm]))

(def ant-button (r/adapt-react-class AntButton))
(def ant-button-group (r/adapt-react-class AntButtonGroup))
(def ant-radio (r/adapt-react-class AntRadio))
(def ant-radio-group (r/adapt-react-class (.-Group AntRadio)))
(def ant-radio-button (r/adapt-react-class (.-Button AntRadio)))
(def ant-icon (r/adapt-react-class AntIcon))
(def ant-row (r/adapt-react-class AntRow))
(def ant-col (r/adapt-react-class AntCol))
(def ant-table (r/adapt-react-class AntTable))
(def ant-dropdown (r/adapt-react-class AntDropdown))
(def ant-menu (r/adapt-react-class AntMenu))
(def ant-menu-item (r/adapt-react-class (.-Item AntMenu)))
(def ant-menu-divider (r/adapt-react-class (.-Divider AntMenu)))
(def ant-input (r/adapt-react-class AntInput))
(def ant-input-search (r/adapt-react-class (.-Search AntInput)))



(def ant-popconfirm (r/adapt-react-class AntPopconfirm))





(def react-ace (r/adapt-react-class ReactAce))

#_(js/console.log
   (js/document.getElementById
    "map-container"))

(defn ol-map
  [x y z]
  (let []
    (r/create-class
     {:display-name "ol-map"
      :component-did-mount
      (fn [this]
        (rf/dispatch [:ged.map-core/create-olmap "map-container"]))
      :component-did-update
      (fn [this old-argv]
        (let [new-argv (rest (r/argv this))]
          (js/console.log new-argv old-argv)))
      :component-will-unmount
      (fn [this]
        (rf/dispatch [:ged.map-core/set-olmap-target nil]))
      :reagent-render
      (fn [x y z]
        [:div#map-container {:style {:width "100%"
                                     :height "100%"
                                     :border "1px solid #dedede"}}])})))


#_(defn ol-wms-layer
  []
  (let []
    (r/create-class
     {:display-name "ol-wms-layer"
      :component-did-mount
      (fn [this]
        (let [{:keys [id geoserver-host]} (r/props this)]
          #_(do (ol/upsert-wms-layer (get-olmap) geoserver-host id))))
      :component-will-unmount
      (fn [this]
        (let [{:keys [id]} (r/props this)]
          (when (get-olmap)
            (do (ol/remove-layer (get-olmap) id)))
          ))
      :reagent-render
      (fn []
        nil)})))

; https://github.com/Day8/re-frame/blob/master/docs/Using-Stateful-JS-Components.md
#_(defn ol-map-layers-inner
  []
  (let []
    (r/create-class
     {:display-name "ol-map-layers"
      :component-did-mount
      (fn [this]
        (let [{:keys [checked-layer-ids geoserver-host]} (r/props this)]
          (js/console.log "view ids" checked-layer-ids)
          (ol/upsert-wfs-layers (get-olmap) geoserver-host checked-layer-ids)
          ))
      :component-did-update
      (fn [this old-argv]
        (let [new-argv (rest (r/argv this))
              {:keys [checked-layer-ids geoserver-host]} (r/props this)]
          (js/console.log "component-did-update" checked-layer-ids)
          #_(js/console.log new-argv old-argv)))
      :component-will-unmount
      (fn [this])
      :reagent-render
      (fn []
        nil)})))

#_(defn ol-map-layers
  []
  (let [geoserver-host (rf/subscribe [:ged.subs/geoserver-host])
        ids-ref (rf/subscribe [::subs/checked-layer-ids])]
    (fn []
      (let [host @geoserver-host
            ids @ids-ref
            ]
        [:<>
         (map (fn [id]
                [ol-wms-layer
                 {:key id
                  :id id
                  :geoserver-host host}])
              ids)]
        
        )
      #_[ol-map-layers-inner
       {:checked-layer-ids @ids-ref
        :geoserver-host @geoserver-host}])))

(defn ol-map-layers-inner
  []
  (let []
    (r/create-class
     {:display-name "ol-map-layers"
      :component-did-mount
      (fn [this]
        (let [{:keys [ids geoserver-host wms-use-auth? credentials]} (r/props this)]
          (rf/dispatch [:ged.map-core/sync-layer-ids [ids {:geoserver-host geoserver-host
                                                           :wms-use-auth? wms-use-auth?
                                                           :credentials credentials}]])))
      :component-did-update
      (fn [this old-argv]
        (let [new-argv (rest (r/argv this))
              {:keys [ids geoserver-host wms-use-auth? credentials]} (r/props this)]
          (rf/dispatch [:ged.map-core/sync-layer-ids [ids {:geoserver-host geoserver-host
                                                           :wms-use-auth? wms-use-auth?
                                                           :credentials credentials}]])
          #_(js/console.log new-argv old-argv)))
      :component-will-unmount
      (fn [this])
      :reagent-render
      (fn []
        nil)})))

(defn ol-map-layers
  []
  (let [geoserver-host (rf/subscribe [:ged.subs/geoserver-host])
        ids-ref (rf/subscribe [::subs/checked-layer-ids])
        awms-use-auth? (rf/subscribe [:ged.settings-subs/wms-use-auth?])
        acredentials (rf/subscribe [:ged.subs/credentials])
        ]
    (fn []
      (let [host @geoserver-host
            ids @ids-ref
            wms-use-auth? @awms-use-auth?
            credentials @acredentials]
        [ol-map-layers-inner {:ids ids
                              :wms-use-auth? wms-use-auth?
                              :geoserver-host host
                              :credentials credentials}]))))

(defn action-buttons
  []
  (let []
    (fn []
      [:section {:style {:position "absolute" :right 64 :top 32}}
       [ant-button-group {:size "default"}
        [ant-button
         {:icon "reload"
          :title "refetch layers"
          :on-click (fn []
                      (rf/dispatch [:ged.map-core/refetch-wms-layers]))}]]])))

(defn active->button-type
  [active?]
  (if active? "primary" "default"))

(defn key->button-type
  [btn-key key]
  (active->button-type (= btn-key key)))

(defn tab-buttons
  []
  (let [atab (rf/subscribe [::subs/tab-button]) ]
    (fn []
      (let [tab @atab]
        [:section {:class "tab-buttons-container"
                   :style {:position "absolute" :left 0 :top 64}}
         [ant-button-group {:style {:display "flex" :flex-direction "column"}
                            :value tab
                            :on-click (fn [ev]
                                        (rf/dispatch
                                         [::evs/tab-button
                                          (.. ev -target -value)]))
                            :size "default"}
          [ant-button {:value "all-layers"
                       :type (key->button-type :all-layers tab)
                       :icon "unordered-list"
                       :title "all layers"}]
          [ant-button {:value "selected-layers"
                       :icon "profile"
                       :type (key->button-type :selected-layers tab)
                       :title "selected layers"}]
          [ant-button {:value "wfs-search"
                       :title "wfs search"
                       :type (key->button-type :wfs-search tab)
                       :icon "search"}]
          [ant-button {:value "modify"
                       :title "modify"
                       :type (key->button-type :modify tab)
                       :icon "edit"}]
          ]])
      )))

;all layers 

(def all-layers-base-colums
  [{:title "name"
    :key :name
    :dataIndex :name}])

(def all-layers-extra-columns
  [{:title ""
    :key "action"
    :width "32px"
    :render
    (fn [txt rec idx]
      (let [on-click
            (fn [ea]
              (let [key (keyword (.-key ea))]
                (cond
                  (= key :select)
                  (do (rf/dispatch
                       [::evs/add-selected-layers-ids [(aget rec "name")]])))))
            menu
            (r/as-element
             [ant-menu {:on-click on-click
                        :size "small"}
              [ant-menu-item {:key "select"} "select"]])]
        (r/as-element
         [ant-dropdown
          {:overlay menu :trigger ["click"]}
          [ant-button
           {:size "small"
            :icon "down"}]])))}])

(def all-layers-colums
  (vec (concat all-layers-base-colums
               all-layers-extra-columns)))

(defn all-layers
  []
  (let [avisible (rf/subscribe [::subs/all-layers-visible])
        adata (rf/subscribe [::subs/all-layers])
        achecked (rf/subscribe [::subs/all-layers-checked])
        table-mdata (rf/subscribe [::subs/all-layers-table-mdata])
        ainput (rf/subscribe [::subs/all-layers-search-input])]
    (fn []
      (let [visible? @avisible
            total (:total @adata)
            input @ainput
            items (:items @adata)
            checked @achecked
            pagination (:pagination @table-mdata)]
        #_(js/console.log "items" items)
        (when visible?
          [:section {:class "all-layers-container"}
           #_[ant-row "all layers"]
           [ant-row
            [ant-col {:style {:text-align "right"}}
             [ant-button-group {:size "small"}
              [ant-button {:icon "reload" :title "update"
                           :on-click
                           #(rf/dispatch
                             [::evs/fetch-all-layers])}]]]]

           [:br]
           [ant-row
            [ant-col
             [ant-input-search {:size "small"
                                :enterButton true
                                :on-search (fn [s] (rf/dispatch
                                                    [::evs/all-layers-search-input s]))}]]]
           [ant-table
            {:show-header true
             :size "small"
             :row-key :name
             :style {:overflow-y "auto" :max-height "90%"}
             :columns all-layers-colums
             :dataSource items
             :on-change (fn [pag fil sor ext]
                          (rf/dispatch [::evs/all-layers-table-mdata
                                        (js->clj {:pagination pag
                                                  :filters fil
                                                  :sorter sor
                                                  :extra ext} :keywordize-keys true)]))
             :scroll {;  :x "max-content" 
                                ;  :y 256
                      }
             :rowSelection {:selectedRowKeys checked
                            :on-change (fn [keys rows ea]
                                         (rf/dispatch
                                          [::evs/all-layers-checked keys])
                                         #_(js/console.log keys rows ea))}
             :pagination (clj->js
                          (merge pagination {:total total
                                             :showTotal (fn [t rng] t)}))}]])))))

; selected layers

(def selected-layers-base-colums
  [{:title "name"
    :key :name
    :dataIndex :name}])


(def selected-layers-extra-columns
  [{:title ""
    :key "action"
    :width "32px"
    :render
    (fn [txt rec idx]
      (let [on-click 
            (fn [ea]
              (let [key (keyword (.-key ea))
                    name (aget rec "name")]
                (cond
                  (= key :remove)
                  (rf/dispatch
                   [::evs/remove-selected-layers-id name])
                  (= key :modify)
                  (rf/dispatch
                   [::evs/modify-layer name]))))
            menu
            (r/as-element
             [ant-menu {:on-click on-click
                        :size "small"}
              [ant-menu-item {:key "modify"} "modify"]
              [ant-menu-divider]
              [ant-menu-item{:key "remove"} "remove"]])]
        (r/as-element
         [ant-dropdown
          {:overlay menu :trigger ["click"]}
          [ant-button
           {:size "small"
            :icon "down"}]]))
      
      )}])

(def selected-layers-colums
  (vec (concat selected-layers-base-colums
               selected-layers-extra-columns)))

(defn selected-layers
  []
  (let [avisible (rf/subscribe [::subs/selected-layers-visible])
        alayers (rf/subscribe [::subs/selected-layers])
        achecked (rf/subscribe [::subs/selected-layers-checked])
        ]
    (fn []
      (let [visible? @avisible
            data @alayers
            checked @achecked]
        (when visible?
          [:section {:class "all-layers-container"}
           [ant-row "selected layers"]
           [ant-row
            [ant-col {:style {:text-align "right"}}
             [ant-button-group {:size "small"}
              #_[ant-button {:icon "reload" :title "update"
                           :on-click
                           #(rf/dispatch
                             [::evs/fetch-all-layers])}]]]]
           [ant-table
            {:show-header true
             :size "small"
             :row-key :name
             :style {:overflow-y "auto" :max-height "94%"}
             :columns selected-layers-colums
             :dataSource data
             :scroll {;  :x "max-content" 
                                ;  :y 256
                      }
             :rowSelection {:selectedRowKeys checked
                            :on-change (fn [keys rows ea]
                                         (rf/dispatch
                                          [::evs/selected-layers-checked keys])
                                         #_(js/console.log keys rows ea))}
             :pagination false}]])))))


(defn wfs-search-layer-input
  []
  (let [ainput (rf/subscribe [::subs/wfs-search-layer-input])]
    (fn []
      (let [input @ainput
            on-change
            (fn [ev]
              (rf/dispatch [::evs/wfs-search-layer-input
                            (.. ev -target -value)]))]
        [ant-input {:value input
                    :size "small"
                    :style {:width "calc(100% - 24px)"}
                    :on-change on-change
                    :placeholder "topp:states"}]))))

(defn wfs-search-feature-ns
  []
  (let [afns (rf/subscribe
              [::subs/wfs-search-fetch-ns])]
    (fn []
      [:div  (or @afns "-")])))

(defn wfs-search-ns-button
  []
  (let []
    (fn []
      [ant-button {:icon "reload"
                   :size "small"
                   :title "resolve layer ns"
                   :on-click (fn []
                               (rf/dispatch [::evs/wfs-search-fetch-ns]))}])))

(defn wfs-search-buttons
  []
  (let [aarea (rf/subscribe [::subs/wfs-search-area-type])]
    (fn []
      (let [area @aarea
            on-click (fn [ev]
                       (rf/dispatch
                        [::evs/wfs-search-area-type
                         (.. ev -target -value)]))]
        [ant-button-group {:size "small" :on-click on-click}
         [ant-button {:title "search a point"
                      :icon "environment"
                      :type (key->button-type :area-point area)
                      :value "area-point"}]
         [ant-button {:title "search an area"
                      :icon "border"
                      :type (key->button-type :area-box area)
                      :value "area-box"}]]))))

(defn map-click-inner
  []
  (let []
    (r/create-class
     {:display-name "map-click-inner"
      :component-did-mount
      (fn [this]
        (let [{:keys [on-click]} (r/props this)]
          (rf/dispatch [:ged.map-core/mapclick-listen
                        {:on-click on-click}])))
      :component-will-unmount
      (fn [this]
        #_(js/console.log "will unmount click")
        (let [{:keys [on-click]} (r/props this)]
          (rf/dispatch [:ged.map-core/mapclick-unlisten
                        {:on-click on-click}])))
      :reagent-render (fn [] nil)})))

(defn wfs-search-map-click
  []
  (let [amap-click? (rf/subscribe [::subs/wfs-search-map-click?])]
    (fn []
      (let [map-click? @amap-click?
            on-click (fn [ev]
                       (rf/dispatch [:ged.map-core/wfs-search-mapclick [ev]]))]
        (when map-click?
          [map-click-inner {:on-click on-click}])))))

(defn wfs-search-area-box-inner
  []
  (let [astate (r/atom nil)]
    (r/create-class
     {:display-name "wfs-search-area-box-inner"
      :component-did-mount
      (fn [this]
        (let [{:keys [on-draw-end]} (r/props this)]
          (rf/dispatch [:ged.map-core/wfs-search-mapbox-listen
                        {:on-draw-end on-draw-end}])))
      :component-will-unmount
      (fn [this]
        (rf/dispatch [:ged.map-core/wfs-search-mapbox-unlisten]))
      :reagent-render (fn [] nil)})))

(defn wfs-search-area-box
  []
  (let [aactive? (rf/subscribe [::subs/wfs-search-area-box?])]
    (fn []
      (let [active? @aactive?
            on-draw-end (fn [ev]
                          (rf/dispatch
                           [:ged.map-core/wfs-search-mapbox [ev]]))]
        (when active?
          [wfs-search-area-box-inner {:on-draw-end on-draw-end}])))))


(def wfs-search-base-columns
  [{:title "id"
    :key "id"
    :dataIndex "id"}
   #_{:title "geometry_name"
    :key "geometry_name"
    :dataIndex "geometry_name"}])

(def wfs-search-extra-columns
  [{:title ""
    :key "action"
    :width "32px"
    :render
    (fn [txt rec idx]
      (let [on-click (fn [ea]
                       (let [key (keyword (.-key ea))
                             name (aget rec "name")]
                         (cond
                           (= key :remove)
                           (rf/dispatch
                            [::evs/remove-selected-layers-id name]))))
            menu (r/as-element
                  [ant-menu {:on-click on-click
                             :size "small"}
                   [ant-menu-item {:key "edit"} "edit"]
                   [ant-menu-divider]
                   [ant-menu-item {:key "remove"} "remove"]])]
        (r/as-element
         [ant-dropdown
          {:overlay menu :trigger ["click"]}
          [ant-button
           {:size "small"
            :icon "down"}]])))}])

(def wfs-search-columns
  (vec (concat
        wfs-search-base-columns
        #_wfs-search-extra-columns)))

(defn wfs-search-table
  []
  (let [adata (rf/subscribe [::subs/wfs-search-res])
        table-mdata (rf/subscribe [::subs/wfs-search-table-mdata])
        aselected-key (rf/subscribe [::subs/wfs-search-selected-key])
        ]
    (fn []
      (let [items (:features @adata)
            total (:totalFeatures @adata)
            ents items
            selected [@aselected-key]
            #_(mapv #(-> % :entity (dissoc :db/id)) items)
            pagination (:pagination @table-mdata)
            first-item (first items)
            ]
        #_(js/console.log first-item)
        [ant-table {:show-header true
                    :size "small"
                    :row-key :id
                    :style {:height "51%" :overflow-y "auto"}
                    :columns wfs-search-columns
                    :dataSource ents
                    ; :defaultExpandedRowKeys [(:id first-item)]
                    :on-change (fn [pag fil sor ext]
                                 (rf/dispatch [::evs/wfs-search-table-mdata
                                               (js->clj {:pagination pag
                                                         :filters fil
                                                         :sorter sor
                                                         :extra ext} :keywordize-keys true)]))
                    :scroll {;  :x "max-content" 
                                ;  :y 256
                             }
                    :rowSelection {:columnWidth "28px"
                                   :selectedRowKeys selected
                                   :on-select (fn [row selected? rows]
                                                (rf/dispatch [::evs/wfs-search-selected-key
                                                              (if selected?
                                                                (aget row "id")
                                                                nil)]))}
                    :defaultExpandAllRows false
                    :expandedRowRender
                    (fn [rec]
                      (r/as-element
                       [:div {:style {:max-height "50vh" :overflow-y "auto"}}
                        (js/JSON.stringify (aget rec "properties") nil "\t")
                        #_(str (js->clj (aget rec "properties")))]))
                    :pagination (merge pagination
                                       {:total total
                                            ; :on-change #(js/console.log %1 %2)
                                        })}]))))

(defn wfs-search-feature-editor-inner
  [opts]
  (let [k-old  (r/atom (:k opts))
        av (r/atom (:value opts))]
    (fn [{:keys [value k]}]
      (when-not (= k @k-old)
        (do
          (reset! k-old k)
          (reset! av value)))
      (let []
        [:<>
         [react-ace {:name "wfs-search-feature-editor"
                     :mode "json"
                     :theme "github"
                     :width "100%"
                     :height "37%"
                    ;  :default-value default-value
                     :value @av
                     :on-change (fn [v ev] (reset! av v))
                     :editor-props {"$blockScrolling" js/Infinity}}]
         [ant-button-group {:size "small" :style {:margin-top 4}}
          [ant-button {:on-click
                       #(rf/dispatch [::evs/wfs-tx {:tx-type :inserts
                                                        :value @av}])
                       :style {:width "96px"}}
           "insert"]
          [ant-button {:on-click
                       #(rf/dispatch [::evs/wfs-tx {:tx-type :updates
                                                        :value @av}])
                       :style {:width "96px"}}
           "update"]
          [ant-popconfirm
           {:title "deelte feature?"
            :on-confirm #(rf/dispatch [::evs/wfs-tx {:tx-type :deletes
                                                         :value @av}])
            :okText "yes" :cancelText "no"}
           [ant-button {:ghost true
                        :type "danger"
                        :style {:width "96px"}}
            "delete"]]]]
        ))))

(defn wfs-search-feature-editor
  []
  (let [afeat (rf/subscribe [::subs/wfs-search-selected-feature])
        ak (rf/subscribe [::subs/wfs-search-selected-key])
        ]
    (fn []
      (let [feat @afeat
            k @ak
            v (if feat (pretty-json (clj->js feat)) "")]
        [wfs-search-feature-editor-inner {:value v :k k}]))))

(defn wfs-search
  []
  (let [avisible (rf/subscribe [::subs/wfs-search-visible])]
    (fn []
      (let [visible? @avisible]
        (when visible?
          [:section {:class "all-layers-container"}
           #_[:div "wfs search"]
           [:div
            [wfs-search-layer-input]
            [wfs-search-ns-button]]
           [ant-row [wfs-search-feature-ns]]
           [ant-row {:style {:margin-top 8}}
            [ant-col {:style {:text-align "right"}}
             [wfs-search-buttons]]]
           
           [wfs-search-table]
           [wfs-search-feature-editor]

           [wfs-search-map-click]
           [wfs-search-area-box]])))))

(defn modify-buttons
  []
  (let [ amodifying? (rf/subscribe [::subs/modifying?])]
    (fn []
      (let [modifying? @amodifying?]
        [ant-button-group {:size "small"}
         #_[ant-button {:title "infer feature namespace"
                      :icon "api"
                      :on-click
                      (fn []
                        (rf/dispatch [::evs/infer-feature-ns ]))
                      :type "default"}]
         [ant-button {:title "commit changes"
                      :icon "save"
                      :on-click 
                      (fn [ev]
                        #_(js/console.log (clj->js (core/get-modify-session)))
                        (rf/dispatch
                           [::evs/tx-features]))
                      :type "default"}]
         [ant-button {:title "cancel modifying"
                      :icon "close-square"
                      :type "default"
                      :ghost false
                      :on-click
                      (fn []
                        (rf/dispatch [::evs/cancel-modifying ]))
                      }]
         ]
        ))))

(defn modify-wfs-click
  []
  (let [amap-click? (rf/subscribe [::subs/modify-wfs-click?])]
    (fn []
      (let [map-click? @amap-click?
            on-click
            (fn [ev]
              (rf/dispatch [:ged.map-core/wfs-modify-mapclick [ev]]))]
        (when map-click?
          [map-click-inner {:on-click on-click}])))))

(defn modify-sync-ol-features-inner
  []
  (let []
    (r/create-class
     {:display-name "modify-sync-ol-features-inner"
      :component-did-mount
      (fn [this]
        (let [{:keys [features]} (r/props this)]
          (rf/dispatch [:ged.map-core/sync-modified-features {:features features}])))
      :component-did-update
      (fn [this]
        (let [{:keys [features]} (r/props this)]
          (rf/dispatch [:ged.map-core/sync-modified-features {:features features}])))
      :component-will-unmount
      (fn [this]
        (let [{:keys [features]} (r/props this)]
          (rf/dispatch [:ged.map-core/sync-modified-features {:features features}])))
      :reagent-render
      (fn []
        nil)})))

(defn modify-sync-ol-features
  []
  (let [afeatures (rf/subscribe [::subs/modified-features])]
    (fn []
      (let [fts @afeatures]
        [modify-sync-ol-features-inner {:features fts}]))))


(defn modify-sync-modify-interactions-inner
  []
  (let []
    (r/create-class
     {:display-name "modify-sync-modify-interactions-inner"
      :component-did-mount
      (fn [this]
        (let [{:keys []} (r/props this)]
          (rf/dispatch [:ged.map-core/add-modify-interactions {}])))
      :component-will-unmount
      (fn [this]
        (let [{:keys []} (r/props this)]
          (rf/dispatch [:ged.map-core/remove-modify-interactions {}])))
      :reagent-render
      (fn []
        nil)})))

(defn modify-sync-modify-interactions
  []
  (let [amodifying? (rf/subscribe [::subs/modifying?])]
    (fn []
      (let [modifying? @amodifying?]
        (when modifying?
          [modify-sync-modify-interactions-inner {}])))))


(defn modify-layer-input
  []
  (let [ainput (rf/subscribe [::subs/modify-layer-id])]
    (fn []
      (let [input @ainput
            on-change
            (fn [ev]
              (rf/dispatch [::evs/modify-layer-id
                            (.. ev -target -value)]))]
        [ant-input {:value input
                    :size "small"
                    :style {:width "calc(100% - 24px)"}
                    :on-change on-change
                    :placeholder "topp:states"}]))))

(defn modify-feature-ns
  []
  (let [afns (rf/subscribe [::subs/modify-layer-ns])]
    (fn []
      [:div  (or @afns "-")])))

(defn modify-ns-button
  []
  (let []
    (fn []
      [ant-button {:icon "reload"
                   :size "small"
                   :title "resolve layer ns"
                   :on-click (fn []
                               (rf/dispatch [::evs/infer-feature-ns]))}])))

(defn modify-modes
  []
  (let [amode (rf/subscribe [::subs/modify-mode])]
    (fn []
      (let [mode @amode]
        [ant-radio-group {:on-change (fn [ev]
                                       (rf/dispatch
                                        [::evs/modify-mode (keyword (.. ev -target -value))]))
                          :size "small"
                          :value (name mode)
                          :default-value "searching"}
         [ant-radio-button {:value "searching"} "selecting"]
         [ant-radio-button {:value "modifying"} "modifying"]]))))

(def modify-features-table-columns
  [{:title "id"
    :key :id
    :dataIndex :id}
   {:title ""
    :key "action"
    :width "32px"
    :render
    (fn [t r i]
      (r/as-element
       [ant-button
        {:size "small"
         :icon "minus"
         :title "remove feature from modified"
         :on-click (fn []
                     (rf/dispatch [::evs/modified-features-remove
                                   (aget r "id")]))}]))}])

(defn modify-features-table
  []
  (let [adata (rf/subscribe [::subs/modified-features])
        aselected-key (rf/subscribe [::subs/modified-features-selected-key])]
    (fn []
      (let [items (-> @adata (vals) (vec))
            selected [@aselected-key]]
        [ant-table {:show-header true
                    :size "small"
                    :row-key :id
                    :style {:height "51%" :overflow-y "auto"}
                    :columns modify-features-table-columns
                    :dataSource items
                    :scroll {;  :x "max-content" 
                                ;  :y 256
                             }
                    :rowSelection {:columnWidth "28px"
                                   :selectedRowKeys selected
                                   :on-select (fn [row selected? rows]
                                                (rf/dispatch [::evs/modified-features-selected-key
                                                              (if selected?
                                                                (aget row "id")
                                                                nil)]))}
                    :defaultExpandAllRows false
                    :expandedRowRender
                    (fn [rec]
                      (r/as-element
                       [:div {:style {:max-height "50vh" :overflow-y "auto"}}
                        (js/JSON.stringify (aget rec "properties") nil "\t")
                        #_(str (js->clj (aget rec "properties")))]))
                    :pagination false}]))))

(defn modify
  []
  (let [avisible (rf/subscribe [::subs/modify-visible])
        ]
    (fn []
      (let [visible? @avisible]
        (when visible?
          [:section {:class "all-layers-container"}
           #_[:div "modify"]
           [:div
            [modify-layer-input]
            [modify-ns-button]]
           [ant-row [modify-feature-ns]]
           [:br]
           [ant-row {:type "flex" :justify "center"}
            [modify-modes]]
           [ant-row {:style {:margin-top 8}}
            [ant-col {:style {:text-align "right"}}
             [modify-buttons]]]
           [:br]
           [modify-features-table]
           
           
           #_[ant-row
            [ant-col
             [:span "layer:  "]
             [:b {:style {;:border-bottom "1px solid #dedede"
                          }}input]


             #_[ant-input {:value input
                           :read-only true
                        ;  :disabled true
                           :placeholder "topp:states"
                           :style {:width "100%"}}]]]
           #_[ant-row
            [ant-col
             [:span "feature-ns:  "]
             [:span layer-ns]]]
           [modify-wfs-click]
           [modify-sync-ol-features]
           [modify-sync-modify-interactions]
           ])))))


(defn panel []
  (let [module-count @(rf/subscribe [::subs/module-count])
        base-url @(rf/subscribe [:ged.subs/base-url])
        ]
    [:div {:style {:height "100%" :margin-left 0}}
     [action-buttons]
     [tab-buttons]
     [ol-map 1 2 3]
     [ol-map-layers]
     [all-layers]
     [selected-layers]
     [wfs-search]
     [modify]
     ]))

