(ns gaz.layer
  (:require
    [gaz.renderable :refer [RenderableProto]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Layer proto and game and hud layers
(defprotocol LayerProto
  (get-scene [this])
  (get-cam [this])
  (add [this obj]))

(defrecord Layer [renderer scene cam]
  
  LayerProto
  (add [_ obj]
    (.add scene obj))

  (get-scene [_] scene)
  (get-cam [_] cam)

  RenderableProto
  (render [this ]
    (.render renderer scene cam)))

(defn- aspect [width height] (/ width height))

(defn mk-main-layer [renderer width height]
  (let [cam (js/THREE.PerspectiveCamera. 25 (aspect width height) 0.1 1000)]
  (set! (.-position cam) (js/THREE.Vector3. 0 0 5))
    (Layer. renderer (js/THREE.Scene.) cam )))

(defn mk-hud-layer [renderer width height]
  (let [ half-ar (/ (aspect width height) 2.0)
        cam     (js/THREE.OrthographicCamera.
                  (- half-ar) half-ar 0.5 -0.5
                  0.1 1000) ]

    (Layer. renderer (js/THREE.Scene.) cam))) 

