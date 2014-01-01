(ns gaz.layer
  (:require
    [gaz.renderable :refer [RenderableProto]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Layer proto and game and hud layers
(defprotocol LayerProto
  (get-scene [this])
  (add [this obj]))

(defrecord Layer [scene cam]
  
  LayerProto
  (add [_ obj]
    (.add scene obj))

  (get-scene [_]
    scene)

  RenderableProto
  (render [this renderer]
    (.render renderer scene cam)))

(defn- aspect [width height] (/ width height))

(defn mk-main-layer[width height]
  (let [cam (THREE.PerspectiveCamera. 75 (aspect width height) 0.1 1000)]
    (Layer. (THREE.Scene.) cam )))

(defn mk-hud-layer [width height]
  (let [ half-ar (/ (aspect width height) 2.0)
        cam     (THREE.OrthographicCamera.
                  (- half-ar) half-ar 0.5 -0.5
                  0.1 1000) ]

    (Layer. (THREE.Scene.) cam))) 

