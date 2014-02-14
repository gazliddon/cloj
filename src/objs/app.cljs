(ns objs.app
  (:require 
    [lt.object      :as object]
    [objs.settings  :as settings]
    [objs.cubegeo    :as cubegeo]
    [cloj.jsutil    :as jsu :refer [log]])

  (:require-macros
    [lt.macros :refer [behavior]]))

(behavior ::init!
          :triggers #{:init!}
          :reaction (fn [this]
                      (let [scene (js/THREE.Scene.)
                            geo (cubegeo/mk-geo-cube scene)]
                        (object/merge! this {:geo   geo
                                             :scene scene }))


                      (log "reacting to an init! from an app")))

(behavior ::update
          :triggers #{:update}
          :reaction (fn [this tm]
                      (let [cur @this]
                        (object/raise :update (:geo cur) tm))
                      ))
(behavior ::render
          :triggers #{:render}
          :reaction  (fn [this]))

(object/object* ::app
                :tags #{:app}
                :init (fn [this]
                        (jsu/log (str "hello from ::app init for " this)))
                )

(def app (object/create ::app))

