(ns objs.settings
  (:require-macros
    [cljs.core.async.macros    :refer [go go-loop]]
    [lt.macros                 :refer [behavior]])
  
  (:require
    [cljs.core.async            :as ca :refer [chan <! >! close!]]
    [file.loader                :as loader ]
    [cloj.jsutil                :as jsu :refer [log]]
    [lt.object                  :as object]))

(behavior ::load-behaviors
          :triggers #{:behaviors.load}
          :reaction (fn [this file]
                      (log "Been told to load " file)
                      (go
                        (object/raise this :behaviors.preload file)
                        (let [load-chan (loader/load file)
                              txt (<! load-chan)]
                          (log "Yeah here we are! having loaded")
                          (object/raise this :behaviors.parse txt)
                          (log "and we're back from asking to parse")
                          (close! load-chan)
                          ))))

(behavior ::parse-behaviors
          :triggers #{:behaviors.parse}
          :reaction (fn [this txt]
                      (log "Been asked to parse")
                      (log txt)
                      (object/raise this :behaviors.loaded)))


