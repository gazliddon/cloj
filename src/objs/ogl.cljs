;; Object to handle to open gl window and stuff

(ns objs.ogl
  (:require-macros
    [cljs.core.async.macros     :refer [go go-loop]]
    [lt.macros                  :refer [behavior]])
  
  (:require
    [cloj.jsutil                  :as jsu :refer [log strlog]]
    [lt.object                    :as object]
    ))

(def render-opts
  (js-obj
    "antialias" false
    "alpha" true
    "stencil" false))

(defn- init-renderer [renderer]
  (doto renderer
    (aset "autoClear" false)
    (aset "autoClearStencil" true)
    (aset "autoClearDepth" true)
    (aset "autoClearColor" true)
    )
  renderer)

(defn- mk-renderer []
  (init-renderer (js/THREE.WebGLRenderer. render-opts)))

(object/object* ::settings
                :tags #{:ogl :resize}
                :width 0
                :height 0
                :renderer nil
                )

(behavior ::create!
          :triggers #{:init!}
          :reaction (fn [this width height elem]
                      (object/merge! this {:renderer (mk-renderer)})
                      (object/raise this :resize width height)
                      (.appendChild elem (.-domElement (:renderer @this)))
                      ))

(behavior ::resize!
          :triggers #{:resize}
          :reaction (fn [this width height]
                      (object/merge! this {:width width
                                           :height height })
                      (.setSize (:renderer @this) width height)))

