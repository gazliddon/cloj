(ns gaz.rendertarget
  )

(def ^:dynamic *current-rt* nil)

(defn- mk-shader-mat [src-texture]
  (js/THREE.MeshPhongMaterial. 
    (js-obj "color" 0xffffffff
            "shininess" 100
            "map" src-texture)))

(defrecord RenderTarget [render-target material width height])

(defn get-current-render-target [] *current-rt*)

(defn get-render-target [rt] (:render-target rt))

(defn mk-render-target [width height]
  (let [ rt-rt    (js/THREE.WebGLRenderTarget.
                    width height
                    (js-obj "format"        js/THREE.RGBFormat
                            "stencilBuffer" false))

        rt-mat   (mk-shader-mat rt-rt) ]

    (RenderTarget. rt-rt rt-mat width height)) )

