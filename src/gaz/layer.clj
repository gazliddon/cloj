(ns gaz.layer
  (:require
    [gaz.layerproto :refer [LayerProto]]
    [gaz.three :refer [set-pos!]]
    [cloj.jsutil :as jsu]
    [gaz.rendertarget :refer [*current-rt*]]
    [gaz.renderable :refer [RenderableProto get-renderer]]))

(defn render-with-current-rt [scene cam]
  (let [r (get-renderer)]
    (if (nil? *current-rt*)
      (.render r scene cam)
      (.render r scene cam *current-rt*)))
  )
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
    (render-with-current-rt scene cam)
    ))

(defn mk-perspective-cam [width height fov pos]
  (let [cam (js/THREE.PerspectiveCamera. fov (/ width height) 0.1 10000)]
    (set-pos! cam pos)
    cam))

(defn mk-perspective-layer [width height fov pos]
  (Layer. (js/THREE.Scene.) (mk-perspective-cam width height fov pos)))



;; ends
