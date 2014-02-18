(ns objs.feedback
  (:require-macros
    [lt.macros                  :refer [behavior]])

  (:require
    [gaz.three              :as three]
    [render.shader          :as shader]
    [lt.object              :as object :refer [raise create object* merge!]]
    [cloj.jsutil            :as jsu :refer [log strlog]]
    [objs.cam               :as cam]))

(def default-opts {:format        js/THREE.RGBFormat
                   :stencilBuffer false
                   :anistropy     false  })

(defn- mk-render-target [width height & [opts]]
  (let [opts (or opts default-opts)
        opts (merge default-opts opts)
        rt-rt    (js/THREE.WebGLRenderTarget.  width height (clj->js opts))]
    (aset rt-rt "generateMipMaps" false)
    rt-rt))

(behavior ::flip!
          :triggers #{:flip!}
          :reaction (fn [this]
                      (let [{:keys [last-rt layer]} @this
                            this-rt (raise layer :get-rt)]
                        (raise layer :set-rt! last-rt)
                        (raise this :set-rt! this-rt))))

(behavior ::set-rt!
          :triggers #{:set-rt!}
          :reaction (fn [this rt] (merge! this {:last-rt rt})))

(behavior ::get-rt
          :triggers #{:get-rt}
          :reaction (fn [this] (:last-rt @this)))

(behavior ::render-with-rt
          :triggers #{:render}
          :reaction (fn [this rt [dt t]]
                      (let [{:keys [shader plane last-rt layer]} @this
                            material (:material shader)
                            uniforms (:uniforms)
                            this-rt (raise layer :get-rt) ]

                        (doto uniforms
                          (aset "time"       t)
                          (aset "lastScreen" last-rt)
                          (aset "thisSceeen" this-rt)
                          (aset "inputScree" rt))

                        (raise layer :render)
                        )))

(behavior ::set-effect!
          :triggers #{:set-effect!}
          :reaction (fn [this effect]
                      (let [layer    (:layer @this)
                            shader   (shader/mk-material effect)
                            material (:material shader)
                            plane    (js/THREE.Mesh.
                                       (js/THREE.PlaneGeometry. 1 1 1 1)
                                       material)]
                        (doto material
                          (aset "depthTest" false)
                          (aset "depthWrite" false))
                        (raise layer :add! plane)
                        (merge! this {:shader shader :plane  plane}))))

(object* ::feedback
         :tags #{:feedback}
         :init (fn [this effect width height ]
                 (merge!
                   this {:layer   (create :objs.layer/layer-ortho width height)
                         :last-rt (mk-render-target 1024 1024) })
                 (raise this :set-effect! effect)))

