(ns Leito.feats-sample
  (:require [reagent.core :as r]
            [cljs.repl :as repl]
            [cljs.pprint :as pp]
            [re-frame.core :as rf]
            ["antd/lib/table" :default AntTable]))

(def ant-table (r/adapt-react-class AntTable))


(def data-source 
  [{:key "1"
    :name "Mike"
    :age 32
    :address "10 Downing Street"}
   {:key "2"
    :name "John"
    :age 43
    :address "11 Downing Street"}
   ]
  )

(def columns
  [{:title "Name"
    :dataIndex :name
    :key "name"}
   {:title "Age"
    :dataIndex "age"
    :key "age"}
   {:title "Address"
    :dataIndex "address"
    :key "address"}
   ]
  )

(def data-source-2
  [{"key" "1"
    "name" "Mike"
    "age" 32
    "address" "10 Downing Street"}
   {"key" "2"
    "name" "John"
    "age" 42
    "address" "11 Downing Street"}])

(def columns-2
  [{"title" "Name"
    "dataIndex" "name"
    "key" "name"}
   {"title" "Age"
    "dataIndex" "age"
    "key" "age"}
   {"title" "Address"
    "dataIndex" "address"
    "key" "address"}])

(defn sample-table
  []
  (let []
    (fn []
      [ant-table
       {:dataSource data-source
        :columns columns
        }])))



;; people data
#_(def people-data [{:id 1 :name "Tracey Davidson" :age 43 :address "5512 Pockrus Page Rd"}
                    {:id 2 :name "Pierre de Wiles" :age 41 :address "358 Fermat's St"}
                    {:id 3 :name "Lydia Weaver" :age 23 :address "1251 Fourth St"}
                    {:id 4 :name "Willie Reynolds" :age 26 :address "2984 Beechcrest Rd"}
                    {:id 5 :name "Richard Perelman" :age 51 :address "2003 Poincaré Ricci Rd"}
                    {:id 6 :name "Srinivasa Ramanujan" :age 32 :address "1729 Taxi Cab St"}
                    {:id 7 :name "Zoe Cruz" :age 31 :address "8593 Pine Rd"}
                    {:id 8 :name "Adam Turing" :age 41 :address "1936 Automata Lane"}])

#_(defn comparison [data1 data2 field]
    (compare (get (js->clj data1 :keywordize-keys true) field)
             (get (js->clj data2 :keywordize-keys true) field)))


;; we need to use dataIndex instead of data-index, see README.md
#_(def people-columns [{:title "Name" :dataIndex "name" :sorter #(comparison %1 %2 :name)}
                       {:title "Age" :dataIndex "age" :sorter #(comparison %1 %2 :age)}
                       {:title "Address" :dataIndex "address" :sorter #(comparison %1 %2 :address)}])

#_(def pagination {:show-size-changer true
                   :default-page-size 10
                   :page-size-options ["5" "10" "20"]
                   :position "top"
                   :show-total #(str "Total: " % " entities")})


#_(def columns [{:title "attr1" :data-index "attr1" :key "attr1"}
                {:title "attr2" :data-index "attr2" :key "attr2"}
                {:title "attr3" :data-index "attr3" :key "attr3"}])