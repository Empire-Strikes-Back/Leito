(ns Leito.David.db
  (:require [clojure.spec.alpha :as s]
            [re-frame.core :as rf]
            ))

(defn gen-default-conf
  "Returns the default api conf"
  []
  (let [api-v1-baseurl ""]
    {:api.v1/base-url api-v1-baseurl}))

(defn gen-default-db
  "gens the deafult db"
  []
  (let [conf (gen-default-conf)
        base-url (:api.v1/base-url conf)]
    {; core

     :Leito.David.db.core/name "we improvise"
    ;  :Leito.David.db.core/count 0
    ;  :Leito.David.db.core/module-count 0
    ;  :Leito.David.db.core/active-panel nil
     :Leito.David.db.core/conf conf
     :Leito.David.db.core/api {:base-url base-url
                       :search (str base-url "/usda/search")}

     :Leito.David.db.core/username "admin"
     :Leito.David.db.core/password "myawesomegeoserver"

     :Leito.David.db.core/active-profile-key 0

     :Leito.David.db.core/profiles (into (sorted-map)
                                 {0 {:key 0
                                     :host "http://localhost:8600/geoserver"
                                     :proxy-host  "http://geoserver:8080/geoserver"
                                     :username "admin"
                                     :password "myawesomegeoserver"}
                                  1 {:key 1
                                     :host "http://localhost:8600/geoserver"
                                     :proxy-host  "http://geoserver:8080/geoserver"
                                     :username "admin"
                                     :password "myawesomegeoserver"}
                                  2 {:key 2
                                     :host "https://example.com/geoserver"
                                     :proxy-host  "https://example.com/geoserver"
                                     :username "admin"
                                     :password "geoserver"}})

     :Leito.David.db.core/proxy-path "/geoserver"

     :Leito.David.db.core/log-que []
     :Leito.David.db.log/log-table-mdata {:pagination {:showSizeChanger false
                                               :showQuickJumper false
                                               :defaultPageSize 5
                                               :pageSizeOptions  ["5" "10" "20"]
                                               :position "top"
                                               :total 0
                                               :current 1
                                               :pageSize 22}
                                       :filters {}
                                       :sorter {}
                                       :extra {:currentDataSource []}}
     :Leito.David.db.log/selected-item-key nil

      ; feats
     :Leito.David.db.feats/search-res nil
     :Leito.David.db.feats/search-input ""
     :Leito.David.db.feats/select-feature nil
     :Leito.David.db.feats/tx-res nil
     :Leito.David.db.feats/search-table-mdata {:pagination {:showSizeChanger false
                                                    :showQuickJumper false
                                                    :defaultPageSize 5
                                                    :pageSizeOptions  ["5" "10" "20"]
                                                    :position "top"
                                                    :total 0
                                                    :current 1
                                                    :pageSize 10}
                                       :filters {}
                                       :sorter {}
                                       :extra {:currentDataSource []}}
     :Leito.David.db.feats/feature-type-input "" #_"dev:usa_major_cities"
     :Leito.David.db.feats/feature-ns "" #_"http://www.opengis.net/wfs/dev"
     :Leito.David.db.feats/fetch-ftype-mdata-ns-res nil
     :Leito.David.db.feats/fetch-ftype-mdata-layer-res nil

     :Leito.David.db.feats/selected-attrs []
     :Leito.David.db.feats/use-eqcl-filter? false


     ; map

     :Leito.David.db.map/tab-button nil
     :Leito.David.db.map/fetch-all-layers-res nil

     :Leito.David.db.map/all-layers-table-mdata {:pagination {:showSizeChanger false
                                                      :showQuickJumper false
                                                      :defaultPageSize 5
                                                      :pageSizeOptions  ["5" "10" "20"]
                                                      :position "top"
                                                      :total 0
                                                      :showLessItems true
                                                      :current 1
                                                      :pageSize 24}
                                         :filters {}
                                         :sorter {}
                                         :extra {:currentDataSource []}}

     :Leito.David.db.map/selected-layers-ids [#_"dev:usa_major_cities"
                                      #_"dev:usa_major_highways"
                                      #_"dev:world_cities"
                                      #_"dev:world_continents"]
     :Leito.David.db.map/selected-layers-checked []
     :Leito.David.db.map/all-layers-checked []

     :Leito.David.db.map/wfs-search-layer-input ""
     :Leito.David.db.map/wfs-search-area-type nil
     :Leito.David.db.map/wfs-search-table-mdata {:pagination {:showSizeChanger false
                                                      :showQuickJumper false
                                                      :defaultPageSize 5
                                                      :pageSizeOptions  ["5" "10" "20"]
                                                      :position "top"
                                                      :total 0
                                                      :current 1
                                                      :pageSize 15}
                                         :filters {}
                                         :sorter {}
                                         :extra {:currentDataSource []}}



     :Leito.David.db.map/wfs-search-res nil
     :Leito.David.db.map/wfs-search-last-filter nil

     :Leito.David.db.map/modify-layer-id "" #_"dev:usa_major_cities"
     :Leito.David.db.map/modify-layer-ns "" #_"http://www.opengis.net/wfs/dev"
     :Leito.David.db.map/modify-wfs-click-last-filter nil
     :Leito.David.db.map/modify-wfs-click-res nil
     :Leito.David.db.map/modifying? false
     :Leito.David.db.map/modify-mode :searching
     :Leito.David.db.map/modified-features {}
     :Leito.David.db.map/modified-features-selected-key nil
     
     
     :Leito.David.db.map/tx-res nil
     :Leito.David.db.map/all-layers-search-input ""
     :Leito.David.db.map/infer-feature-ns-res nil
     :Leito.David.db.map/wfs-search-selected-key nil
     :Leito.David.db.map/wfs-tx-res nil
     :Leito.David.db.map/wfs-search-fetch-ns-res nil

     ; rest
     :Leito.David.db.rest/fetch-selected-url-res nil
     :Leito.David.db.rest/selected-url "/rest/workspaces/dev/featuretypes.json"
     :Leito.David.db.rest/select-item nil
     :Leito.David.db.rest/tx-res nil
     :Leito.David.db.rest/search-table-mdata {:pagination false
                                      #_{:showSizeChanger false
                                         :showQuickJumper false
                                         :defaultPageSize 5
                                         :pageSizeOptions  ["5" "10" "20"]
                                         :position "top"
                                         :total 0
                                         :current 1
                                         :pageSize 10}
                                      :filters {}
                                      :sorter {}
                                      :extra {:currentDataSource []}}
     :Leito.David.db.rest/selected-item-href nil
     :Leito.David.db.rest/selected-item-path nil
     :Leito.David.db.rest/layer-id-input ""
     
     

     ; settings 
     :Leito.David.db.settings/wms-use-auth? true
     :Leito.David.db.settings/geometry-name "the_geom"


    ;
     }))




(def default-db (gen-default-db))

#_(get-in default-db [:Leito.David.db.core/api :base-url] )