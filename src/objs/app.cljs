(ns objs.app
  (:require 
    [lt.object                   :as object :refer [raise create object* merge!]]
    [objs.settings               :as settings]
    [objs.cubegeo                :as cubegeo])

  (:require-macros
    [lt.macros :refer [behavior]]))

(behavior ::update
          :triggers #{:update}
          :reaction (fn [this tm]
                      (let [cur @this]
                        (raise :update (:geo cur) tm))
                      ))
(behavior ::render
          :triggers #{:render}
          :reaction  (fn [this]))

(object* ::app
         :tags #{:app}
         :init (fn [this]
                 (let [scene (js/THREE.Scene.)
                       geo (cubegeo/mk-cube-geo scene 100)]
                   (merge! this {:geo   geo
                                 :scene scene }))))

(def app (create ::app))

