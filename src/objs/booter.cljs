;; This where we boot the app
  ;; Load in behaviors
  ;; And then kick off the app
(ns objs.booter
  (:require-macros
    [cljs.core.async.macros     :refer [go go-loop]]
    [lt.macros                  :refer [behavior]]
    )
  
  (:require
    [cloj.jsutil                      :as jsu :refer [log]]
    [objs.app                         :as app]
    [objs.loader                      :as loader]
    [objs.settings                    :as settings]
    [lt.object                        :as object]))

(behavior ::begin-boot
          :triggers #{:boot}
          :reaction (fn [this]
                      (log "beggining boot")
                      (object/raise this :file.load "settings.clj")))

(behavior ::settings-loaded
          :triggers #{:file.onload}
          :reaction (fn [this txt file]
                      (log "settings loaded in boot")
                      (object/raise this :settings.parse txt file)
                      (object/raise app/app :init!)))

(def booter (object/create (object/object* ::booter :tags #{:booter })))

(object/tag-behaviors
  :booter [::begin-boot
           ::settings-loaded
           :objs.settings/parse-settings
           :objs.loader/loader ])

(defn boot []
  (do
    (log "about to booter")
    (object/raise booter :boot)
    (log "booter off and running")))

