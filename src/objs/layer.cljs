(ns objs.layer
  (:require
    [lt.object    :as object]
    [gaz.three    :refer [set-pos!]]
    [cloj.jsutil  :as jsu :refer [log]])

  (:require-macros
    [lt.macros                  :refer [behavior]]))

(def default-opts {:clear       false
                   :clear-color 0xff00ff
                   :alpha       1
                   :rt-width    1024
                   :rt-height   1024 })

(def default-rt-opts {:format js/THREE.RGBFormat
                      :stencilBuffer false
                      :anistropy false})

(defn- mk-rt [w h & [ opts]]
  (let [opts (merge opts default-rt-opts)
        rt (js/THREE.WebGLRenderTarget.  w h (clj->js opts))]
    (aset rt "generateMipMaps" false)
    rt))

(behavior ::add!
          :triggers #{:add!}
          :reaction (fn [this obj]
                      (.add (:scene @this) obj)))

(behavior ::render
          :triggers #{:render}
          :reaction (fn [this renderer]
                      (let [{:keys [scene three-cam render-target
                                     clear-color alpha clear]} @this]
                        (.setClearColor renderer clear-color alpha)
                        (.render scene renderer three-cam render-target clear))))

(behavior ::set-clear-color!
          :triggers #{:set-color}
          :reaction (fn [this col]
                      (object/merge! this {:clear-color col})))

(behavior ::get-rt
          :triggers #{:results :get-rt}
          :reaction (fn [this ]
                      (:three-rt @this)))

(object/object* ::layer-base
                :tags #{:layer}
                :init (fn [this cam & [opts]]
                        (let [opts (merge default-opts (or opts default-opts))
                              {:keys [rt-width rt-height]} opts]
                          (object/merge!
                            this {:three-scene (js/THREE.Scene.)
                                  :three-rt (mk-rt rt-width rt-height)}))))

(defn- mk-perspective-cam [width height fov]
  (let [cam (js/THREE.PerspectiveCamera. fov (/ width height) 0.1 10000)]
    cam))

(defn- mk-orho-cam [width height]
  (let [wd2 (/ width 2.0) hd2 (/ height 2.0)
        cam (js/THREE.OrthographicCamera. (- wd2) wd2 hd2 (- hd2) 0.1 10000)]
    (set-pos! cam (array 0 0 1))
    cam)) 

(defn- cam-layer-object! [this cam pos]
  (set-pos! cam pos )
  (object/merge this (object/create ::layer-base cam)))

(object/object* ::layer-persp
                :tags #{:layer}
                :init (fn [this w h fov pos]
                        (let [cam (mk-perspective-cam w h fov)]
                          (cam-layer-object! this cam pos))))

(object/object* ::layer-ortho
                :tags #{:layer}
                :init (fn [this w h]
                        (cam-layer-object! this (mk-orho-cam w h) (array 0 0 1))))

