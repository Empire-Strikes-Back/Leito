(ns Leito.db
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

     :Leito.db.core/name "we improvise"
    ;  :Leito.db.core/count 0
    ;  :Leito.db.core/module-count 0
    ;  :Leito.db.core/active-panel nil
     :Leito.db.core/conf conf
     :Leito.db.core/api {:base-url base-url
                       :search (str base-url "/usda/search")}

     :Leito.db.core/username "admin"
     :Leito.db.core/password "myawesomegeoserver"

     :Leito.db.core/active-profile-key 0

     :Leito.db.core/profiles (into (sorted-map)
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

     :Leito.db.core/proxy-path "/geoserver"

     :Leito.db.core/log-que []
     :Leito.db.log/log-table-mdata {:pagination {:showSizeChanger false
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
     :Leito.db.log/selected-item-key nil

      ; feats
     :Leito.db.feats/search-res nil
     :Leito.db.feats/search-input ""
     :Leito.db.feats/select-feature nil
     :Leito.db.feats/tx-res nil
     :Leito.db.feats/search-table-mdata {:pagination {:showSizeChanger false
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
     :Leito.db.feats/feature-type-input "" #_"dev:usa_major_cities"
     :Leito.db.feats/feature-ns "" #_"http://www.opengis.net/wfs/dev"
     :Leito.db.feats/fetch-ftype-mdata-ns-res nil
     :Leito.db.feats/fetch-ftype-mdata-layer-res nil

     :Leito.db.feats/selected-attrs []
     :Leito.db.feats/use-eqcl-filter? false


     ; map

     :Leito.db.map/tab-button nil
     :Leito.db.map/fetch-all-layers-res nil

     :Leito.db.map/all-layers-table-mdata {:pagination {:showSizeChanger false
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

     :Leito.db.map/selected-layers-ids [#_"dev:usa_major_cities"
                                      #_"dev:usa_major_highways"
                                      #_"dev:world_cities"
                                      #_"dev:world_continents"]
     :Leito.db.map/selected-layers-checked []
     :Leito.db.map/all-layers-checked []

     :Leito.db.map/wfs-search-layer-input ""
     :Leito.db.map/wfs-search-area-type nil
     :Leito.db.map/wfs-search-table-mdata {:pagination {:showSizeChanger false
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



     :Leito.db.map/wfs-search-res nil
     :Leito.db.map/wfs-search-last-filter nil

     :Leito.db.map/modify-layer-id "" #_"dev:usa_major_cities"
     :Leito.db.map/modify-layer-ns "" #_"http://www.opengis.net/wfs/dev"
     :Leito.db.map/modify-wfs-click-last-filter nil
     :Leito.db.map/modify-wfs-click-res nil
     :Leito.db.map/modifying? false
     :Leito.db.map/modify-mode :searching
     :Leito.db.map/modified-features {}
     :Leito.db.map/modified-features-selected-key nil
     
     
     :Leito.db.map/tx-res nil
     :Leito.db.map/all-layers-search-input ""
     :Leito.db.map/infer-feature-ns-res nil
     :Leito.db.map/wfs-search-selected-key nil
     :Leito.db.map/wfs-tx-res nil
     :Leito.db.map/wfs-search-fetch-ns-res nil

     ; rest
     :Leito.db.rest/fetch-selected-url-res nil
     :Leito.db.rest/selected-url "/rest/workspaces/dev/featuretypes.json"
     :Leito.db.rest/select-item nil
     :Leito.db.rest/tx-res nil
     :Leito.db.rest/search-table-mdata {:pagination false
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
     :Leito.db.rest/selected-item-href nil
     :Leito.db.rest/selected-item-path nil
     :Leito.db.rest/layer-id-input ""
     
     

     ; settings 
     :Leito.db.settings/wms-use-auth? true
     :Leito.db.settings/geometry-name "the_geom"


    ;
     }))




(def default-db (gen-default-db))

#_(get-in default-db [:Leito.db.core/api :base-url] )