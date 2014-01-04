(ns gaz.rendertarget

  (:require
    [cloj.jsutil       :as jsu]
    [gaz.renderable    :refer [render RenderableProto]]
    [gaz.layer         :refer [mk-main-layer LayerProto get-scene]]
    [gaz.math2         :as math]
    [gaz.three         :refer [set-pos!]]
    ))


(defn- mk-shader-mat [src-texture]
  (js/THREE.MeshPhongMaterial. 
    (clj->js {"color" 0xffffffff "shininess" 100 "map" src-texture})) )

(defrecord RenderTarget [renderer scene cam render-target material width height]
  LayerProto
  (get-scene [_] scene)
  (add [_ obj] (.add scene obj))
  
  RenderableProto
  (render [this]
    (.render renderer scene cam render-target)))

(defn mk-render-target [renderer width height]
  (let [rt-opts {"format"        js/THREE.RGBFormat
                 "stencilBuffer" false}
      
        rt-cam   (js/THREE.PerspectiveCamera. 50 (/ width height) 0.1 1000)
        rt-rt    (js/THREE.WebGLRenderTarget. width height (clj->js rt-opts))
        rt-mat   (mk-shader-mat rt-rt) ]

    (set-pos! rt-cam (math/mk-vec 0 0 30))
    (RenderTarget. renderer (js/THREE.Scene.) rt-cam rt-rt rt-mat width height)) )

