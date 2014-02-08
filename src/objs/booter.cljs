;; This where we boot the app
  ;; Load in behaviors
  ;; And then kick off the app
(ns objs.booter
  (:require-macros
    [cljs.core.async.macros     :refer [go go-loop]]
    [lt.macros                  :refer [behavior]])
  
  (:require
    [cloj.jsutil                     :as jsu :refer [log]]
    [objs.app                        :as app]
    [objs.settings                   :as settings]
    [lt.object                       :as object]))

(behavior ::begin-boot
          :triggers #{:boot}
          :reaction (fn [this]
                      (log "got my boot msg!")
                      (object/raise this :behaviors.load "settings.clj")))

(behavior ::complete-boot
          :triggers #{:behaviors.loaded}
          :reaction (fn [this]
                      (log "all loaded!")
                      (object/raise app/app :init)))

(object/object* ::booter
                :tags #{:booter})

(def booter (object/create ::booter))

(object/tag-behaviors
  :booter [::begin-boot
           ::complete-boot
           :objs.settings/load-behaviors
           :objs.settings/parse-behaviors
           ])

(defn boot []
  (do
    (log "about to booter")
    (object/raise booter :boot)
    (log "booter off and running")))

