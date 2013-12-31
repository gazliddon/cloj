(ns gaz.layer
  (:require
    [gaz.renderable :refer [RenderableProto]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Layer proto and game and hud layers
(defprotocol LayerProto
  (get-scene [this])
  (add [this obj]))

(defrecord Layer [renderer cam scene]
  LayerProto
  (add [_ obj]
    (.add scene obj))
  (get-scene [_] (scene))

  RenderableProto
  (render [this]
    (.render renderer cam scene)))

(defn- aspect [width height] (/ width height))

(defn mk-main-layer[renderer width height]
  (let [cam (THREE.PerspectiveCamera. 75 (aspect width height) 0.1 1000)]
    (Layer. renderer cam (THREE.Scene.))))

(defn mk-hud-layer [renderer width height]
  (let [ half-ar (/ (aspect width height) 2.0)
        cam     (THREE.OrthographicCamera.
                  (- half-ar) half-ar 0.5 -0.5
                  0.1 1000) ]

    (Layer. renderer cam (THREE.Scene.)))) 

