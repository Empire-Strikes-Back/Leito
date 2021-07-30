(ns ged.db
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

     :ged.db.core/name "ged"
    ;  :ged.db.core/count 0
    ;  :ged.db.core/module-count 0
    ;  :ged.db.core/active-panel nil
     :ged.db.core/conf conf
     :ged.db.core/api {:base-url base-url
                       :search (str base-url "/usda/search")}

     :ged.db.core/username "admin"
     :ged.db.core/password "myawesomegeoserver"

     :ged.db.core/active-profile-key 0

     :ged.db.core/profiles (into (sorted-map)
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

     :ged.db.core/proxy-path "/geoserver"

     :ged.db.core/log-que []
     :ged.db.log/log-table-mdata {:pagination {:showSizeChanger false
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
     :ged.db.log/selected-item-key nil

      ; feats
     :ged.db.feats/search-res nil
     :ged.db.feats/search-input ""
     :ged.db.feats/select-feature nil
     :ged.db.feats/tx-res nil
     :ged.db.feats/search-table-mdata {:pagination {:showSizeChanger false
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
     :ged.db.feats/feature-type-input "" #_"dev:usa_major_cities"
     :ged.db.feats/feature-ns "" #_"http://www.opengis.net/wfs/dev"
     :ged.db.feats/fetch-ftype-mdata-ns-res nil
     :ged.db.feats/fetch-ftype-mdata-layer-res nil

     :ged.db.feats/selected-attrs []
     :ged.db.feats/use-eqcl-filter? false


     ; map

     :ged.db.map/tab-button nil
     :ged.db.map/fetch-all-layers-res nil

     :ged.db.map/all-layers-table-mdata {:pagination {:showSizeChanger false
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

     :ged.db.map/selected-layers-ids [#_"dev:usa_major_cities"
                                      #_"dev:usa_major_highways"
                                      #_"dev:world_cities"
                                      #_"dev:world_continents"]
     :ged.db.map/selected-layers-checked []
     :ged.db.map/all-layers-checked []

     :ged.db.map/wfs-search-layer-input ""
     :ged.db.map/wfs-search-area-type nil
     :ged.db.map/wfs-search-table-mdata {:pagination {:showSizeChanger false
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



     :ged.db.map/wfs-search-res nil
     :ged.db.map/wfs-search-last-filter nil

     :ged.db.map/modify-layer-id "" #_"dev:usa_major_cities"
     :ged.db.map/modify-layer-ns "" #_"http://www.opengis.net/wfs/dev"
     :ged.db.map/modify-wfs-click-last-filter nil
     :ged.db.map/modify-wfs-click-res nil
     :ged.db.map/modifying? false
     :ged.db.map/modify-mode :searching
     :ged.db.map/modified-features {}
     :ged.db.map/modified-features-selected-key nil
     
     
     :ged.db.map/tx-res nil
     :ged.db.map/all-layers-search-input ""
     :ged.db.map/infer-feature-ns-res nil
     :ged.db.map/wfs-search-selected-key nil
     :ged.db.map/wfs-tx-res nil
     :ged.db.map/wfs-search-fetch-ns-res nil

     ; rest
     :ged.db.rest/fetch-selected-url-res nil
     :ged.db.rest/selected-url "/rest/workspaces/dev/featuretypes.json"
     :ged.db.rest/select-item nil
     :ged.db.rest/tx-res nil
     :ged.db.rest/search-table-mdata {:pagination false
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
     :ged.db.rest/selected-item-href nil
     :ged.db.rest/selected-item-path nil
     :ged.db.rest/layer-id-input ""
     
     

     ; settings 
     :ged.db.settings/wms-use-auth? true
     :ged.db.settings/geometry-name "the_geom"


    ;
     }))




(def default-db (gen-default-db))

#_(get-in default-db [:ged.db.core/api :base-url] )