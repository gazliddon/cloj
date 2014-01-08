(ns gaz.layer
  (:require
    [gaz.renderable :refer [RenderableProto get-renderer]]))

(def ^:dynamic *current-rt* nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Layer proto and game and hud layers
(defprotocol LayerProto
  (get-scene [this])
  (get-cam [this])
  (add [this obj]))

(defrecord Layer [scene cam]

  LayerProto
  (add [_ obj]
    (.add scene obj))

  (get-scene [_] scene)
  (get-cam [_] cam)

  RenderableProto
  (render [this ]
    (let [r (get-renderer)] 
      (if (nil? *current-rt*)
        (.render r scene cam)
        (.render r scene cam *current-rt*)))))

(defn- aspect [width height] (/ width height))

(defn mk-main-layer [width height]
  (let [cam (js/THREE.PerspectiveCamera. 25 (aspect width height) 0.1 1000)]
  (set! (.-position cam) (js/THREE.Vector3. 0 0 5))
    (Layer. (js/THREE.Scene.) cam )))

(defn mk-hud-layer [width height]
  (let [ half-ar (/ (aspect width height) 2.0)
        cam     (js/THREE.OrthographicCamera.
                  (- half-ar) half-ar 0.5 -0.5
                  0.1 1000) ]

    (Layer. (js/THREE.Scene.) cam))) 

