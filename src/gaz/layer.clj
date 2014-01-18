(ns gaz.layer
  (:require
    [gaz.layerproto :refer [LayerProto]]
    [gaz.three :refer [set-pos!]]
    [cloj.jsutil :as jsu]
    [gaz.rendertarget :refer [*current-rt*]]
    [gaz.renderable :refer [RenderableProto get-renderer]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Layer proto and game and hud layers
(defrecord Layer [scene cam]

  LayerProto
  (add [_ obj]
    (.add scene obj))

  (get-scene [_] scene)
  (get-cam [_] cam)

  RenderableProto
  (render [this ]
    (let [r        (get-renderer)
          ]
      (if (nil? *current-rt*)
        (do
          (.render r scene cam))
        (do 
          (.render r scene cam *current-rt*)))
      )))

(defn mk-perspective-layer [width height fov pos]
  (let [cam (js/THREE.PerspectiveCamera. fov (/ width height) 0.1 10000)]
    (set-pos! cam pos)
    (Layer. (js/THREE.Scene.) cam)))

