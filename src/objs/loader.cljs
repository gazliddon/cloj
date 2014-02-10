(ns objs.loader
  (:require-macros
    [cljs.core.async.macros    :refer [go go-loop]]
    [lt.macros                 :refer [behavior]])
  
  (:require
    [cljs.core.async            :as ca :refer [chan <! >! close!]]
    [file.loader                :as loader ]
    [cloj.jsutil                :as jsu :refer [log]]
    [lt.object                  :as object]))

(behavior ::loader
          :triggers #{:file.load}
          :reaction (fn [this file]
                      (go
                        (let [load-chan (loader/load file)
                              txt (<! load-chan)]
                          (object/raise this :file.onload txt file)
                          (close! load-chan)))
                      ))
