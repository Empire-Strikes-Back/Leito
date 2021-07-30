(ns ged.async
  (:require [clojure.core.async :as async :refer [chan <! >! close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

#_(chan)

#_(chan 10)

#_(let [c (chan)]
    (close! c))

#_(let [c (chan 10)]
    (>! c "hello")
    (assert (= "hello" (<! c) ))
    (close! c)
    )

#_a/>!
