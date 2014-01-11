(ns gaz.rendertarget

  (:require
    [gaz.renderable    :refer [render RenderableProto get-renderer]]
    [gaz.three         :refer [set-pos!]]
    [gaz.layerproto    :refer [LayerProto]]
    ))

(def ^:dynamic *current-rt* nil)

(defn- mk-shader-mat [src-texture]
  (js/THREE.MeshPhongMaterial. 
    (js-obj "color" 0xffffffff
            "shininess" 100
            "map" src-texture)))

(defrecord RenderTarget [scene cam render-target material width height]
  LayerProto
  (get-scene [_] scene)
  (add [_ obj] (.add scene obj))
  
  RenderableProto
  (render [this]
    (.render (get-renderer) scene cam render-target)))

(defn mk-render-target [width height]
  (let [rt-cam   (js/THREE.PerspectiveCamera. 50 (/ width height) 0.1 1000)
        rt-rt    (js/THREE.WebGLRenderTarget.
                   width height
                   (js-obj "format"        js/THREE.RGBFormat
                           "stencilBuffer" false))

        rt-mat   (mk-shader-mat rt-rt) ]

    (set-pos! rt-cam (array 0 0 30))
    (RenderTarget. (js/THREE.Scene.) rt-cam rt-rt rt-mat width height)) )

