(ns ged.auth-view
  (:require  [reagent.core :as r]
             [re-frame.core :as rf]
             [cljs.repl :as repl]
             [cljs.pprint :as pp]
             [ged.auth-evs :as evs]
             [ged.auth-subs :as subs]
             ["antd/lib/row" :default AntRow]
             ["antd/lib/col" :default AntCol]
             ["antd/lib/select" :default AntSelect]
             ["antd/lib/input" :default AntInput]
             ["antd/lib/button" :default AntButton]
             ["antd/lib/table" :default AntTable]
             ["antd/lib/tag" :default AntTag]
             ["antd/lib/popconfirm" :default AntPopconfirm]
             ["antd/lib/popover" :default AntPopover]
   
   ))


(def ant-row (r/adapt-react-class AntRow ))
(def ant-col (r/adapt-react-class AntCol))
(def ant-select (r/adapt-react-class AntSelect))
(def ant-select-option (r/adapt-react-class (.-Option AntSelect)))
(def ant-input (r/adapt-react-class AntInput))
(def ant-input-password (r/adapt-react-class (.-Password AntInput)))

(def ant-button (r/adapt-react-class AntButton))
(def ant-table (r/adapt-react-class AntTable))
(def ant-tag (r/adapt-react-class AntTag))
(def ant-popconfirm (r/adapt-react-class AntPopconfirm))
(def ant-popover (r/adapt-react-class AntPopover))

(defn profiles-columns
  [{:keys [add-cell-ref
           active-profile-key]}]
  [#_{:title "key"
      :key :key
      :dataIndex :key}
   {:title "host"
    :key :host
    :dataIndex :host
    :render (fn [t r i] (r/as-element
                         [ant-input
                          {:ref (fn [el] (add-cell-ref el r :host))
                           :defaultValue t}]))}
   {:title "proxy-host"
    :key :proxy-host
    :dataIndex :proxy-host
    :render (fn [t r i] (when t
                          (r/as-element
                           [ant-input
                            {:ref (fn [el] (add-cell-ref el r :proxy-host))
                             :defaultValue t}])))}
   {:title "username"
    :key :username
    :align "center"
    :dataIndex :username
    :render (fn [t r i] (when t
                          (r/as-element
                           [ant-input
                            {:ref (fn [el] (add-cell-ref el r :username))
                             :defaultValue t}])))}
   {:title "password"
    :key :password
    :dataIndex :password
    :render (fn [t r i] (when t
                          (r/as-element
                           [ant-input-password
                            {:ref (fn [el] (add-cell-ref el r :password))
                             :visibilityToggle true
                             :defaultValue t}])))}
   {:title ""
    :key :active?
    :align "center"
    :render (fn [t r i] 
              (when (= (aget r "key") active-profile-key)
                (r/as-element
                 [ant-tag {:color "green"} "active"]))
              )}
   {:title ""
    :key "action"
    :width "32px"
    :render
    (fn [txt rec idx]
      (let [on-activate (fn [ea]
                          (let []
                            (rf/dispatch
                             [:ged.evs/activate-profile rec])))
            on-remove (fn [ea]
                        (let []
                          (rf/dispatch
                           [:ged.evs/remove-profile rec])))]
        (r/as-element
         [:section {:style {:display "flex"}}
          [ant-button
           {:size "small"
            :on-click on-activate
            :type "primary"}
           "activate"]
          [ant-popconfirm
           {:title "remove profile?" :on-confirm on-remove :okText "yes" :cancelText "no"}
           [ant-button
            {:size "small"
             :type "default"}
            "del"]]])))}
   ]
  )

(defn create-add-cell-ref
  [at]
  (fn [el r cell-key]
    (let [row-key (aget r "key")]
      (swap! at update-in [row-key] assoc cell-key el))))

(defn cell-refs->data
  [refs]
  (reduce (fn [a [k v]]
            (let [nv (reduce (fn [a1 [k1 v1]]
                               (if (.-input v1)
                                 (assoc a1 k1 (.. v1 -input -value))
                                 (assoc a1 k1 v1)
                                 )) {} (seq v))]
              (assoc a k nv))) {} (seq refs)))

(defn profiles-table
  []
  (let [adata (rf/subscribe [:ged.subs/profiles])
        aapk (rf/subscribe [:ged.subs/active-profile-key])
        refs (atom {})]
    (fn []
      (let [add-cell-ref (create-add-cell-ref refs)
            apk @aapk
            items (vals @adata)]
        [ant-table {:show-header true
                    :size "small"
                    :title (fn [_]
                             (r/as-element
                              [:section
                               [ant-button
                                {:on-click #(rf/dispatch [:ged.evs/add-profile])
                                 :icon "plus" :size "small"
                                 :title "add profile"}]
                               [ant-button
                                {:on-click
                                 (fn []
                                   (rf/dispatch
                                    [:ged.evs/update-profiles (cell-refs->data @refs)]))
                                 :icon "save" :size "small"
                                 :title "save changes"}]]))
                    :row-key :key
                    :style {:height "30%" :overflow-y "auto"}
                    :columns (profiles-columns {:active-profile-key apk
                                                :add-cell-ref add-cell-ref})
                    :dataSource items
                    :bordered true
                    :scroll {;  :x "max-content" 
                                ;  :y 256
                             }
                    :pagination false}]))))


(defn profile-form
  []
  (let [apf (rf/subscribe [:ged.subs/active-profile])
        uname (r/atom nil)
        pass (r/atom nil)]
    (fn []
      (let [username (:user @apf)
            password (:pass @apf)]
        [:section
         #_[:div "auth"]
         [ant-row
          [ant-col {:span 4} "username"]
          [ant-col {:span 8}
           [ant-input {:value (or @uname username)
                       :on-change
                       #(reset! uname (.. % -target -value))}]]]
         [:br]
         [ant-row
          [ant-col {:span 4} "password"]
          [ant-col {:span 8}
           [ant-input-password {:visibilityToggle true
                                :value (or @pass password)
                                :on-change
                                #(reset! pass (.. % -target -value))}]]]

         [:br]

         [ant-row
          [ant-col {:span 12}
           [ant-row {:type "flex" :justify "end"}
            [ant-col {:span 3 :style {:text-align "right"}}
             [ant-button
              {:title "Geoserver auth is stateless, credentials will be used in every request"
               :on-click (fn [] (rf/dispatch
                                 [::evs/login
                                  {:username (or @uname username)
                                   :password (or @pass password)}]))}
              "apply"]]]]]
         ;
         ]
        ;
        )))
  )

(defn panel []
  (let []
    (fn []
      [:section
       [ant-row 
        [ant-col {:span 2} "profiles"]
        [ant-col {:span 1}
         [ant-popover
          {:content
           (r/as-element
            [:div
             [:span "- host and proxy-host can be the same "]
             [:br]
             [:span "- use proxy-host only when geoserver is running inside a container, in the same network as ged "]
             ])}
          [ant-button
           {:icon "question"
            :shape "circle"
            :size "small"
            :style {:margin-left "4px"
                    :width "12px" :height "12px"
                    :font-size "8px" :min-width "initial"}}]]]
        ]
       [:br]
       [profiles-table]
       [:br]
       [:br]
       #_[profile-form]]
      
      )))

