(ns render.rendertarget
  )

(def ^:dynamic *current-rt* nil)
(defn get-current-render-target [] *current-rt*)

(defrecord RenderTarget [render-target width height])
(defn get-render-target [rt] (:render-target rt))

(def default-opts {:format        js/THREE.RGBFormat
                   :stencilBuffer false
                   :anistropy     false})

(defn mk-render-target [width height & [opts]]
  (let [opts (or opts default-opts)
        opts (merge default-opts opts)
        
        rt-rt    (js/THREE.WebGLRenderTarget.
                   width height
                   (clj->js opts))]

    (aset rt-rt "generateMipMaps" false)

    (RenderTarget. rt-rt width height)) )




