(ns gaz.rendertarget
  )

(def ^:dynamic *current-rt* nil)
(defn get-current-render-target [] *current-rt*)

(defrecord RenderTarget [render-target width height])
(defn get-render-target [rt] (:render-target rt))

(defn mk-render-target [width height]
  (let [rt-rt    (js/THREE.WebGLRenderTarget.
                   width height
                   (js-obj
                     "format"        js/THREE.RGBFormat
                     "stencilBuffer" false)) ]

    (RenderTarget. rt-rt width height)) )




