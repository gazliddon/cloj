(ns gaz.layer
  (:require
    [gaz.layerproto :refer [LayerProto]]
    [gaz.three :refer [set-pos!]]
    [cloj.jsutil :as jsu]
    [gaz.rendertarget :refer [*current-rt*]]
    [gaz.renderable :refer [RenderableProto get-renderer]]))

(defn render-with-current-rt-opts [scene cam opts]
  (doto (get-renderer)
      (.setClearColor (:clear-color opts))
      (.render scene cam *current-rt* (:clear opts))
    ))

(def default-opts {:clear false
                  :clear-color 0xff00ff})
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Layer proto and game and hud layers
(defrecord Layer [scene cam opts]

  LayerProto
  (add [_ obj]
    (.add scene obj))

  (get-scene [_] scene)
  (get-cam [_] cam)

  RenderableProto
  (render [this ]
    (do
      (doto (get-renderer)
        (.setClearColor (:clear-color opts))
        (.render scene cam *current-rt* (:clear opts))
        )

      )
    ))

(defn mk-layer
  [scene cam opts]
  (let [opts (merge default-opts opts)]
    (Layer. scene cam opts)))

(defn mk-perspective-cam [width height fov pos]
  (let [cam (js/THREE.PerspectiveCamera. fov (/ width height) 0.1 10000)]
    (set-pos! cam pos)
    cam))

(defn mk-orho-cam [width height]
  (let [wd2 (/ width 2.0) hd2 (/ height 2.0)
        cam (js/THREE.OrthographicCamera. (- wd2) wd2 hd2 (- hd2) 0.1 10000)]
    (set-pos! cam (array 0 0 1))
    cam)) 

(defn mk-ortho-layer
  [width height  & [opts]]
  (mk-layer
    (js/THREE.Scene.)
    (mk-orho-cam width height)
    (or opts default-opts)))

(defn mk-perspective-layer
  [width height fov pos & [opts]]
  (mk-layer
    (js/THREE.Scene.)
    (mk-perspective-cam width height fov pos)
    (or opts default-opts)))

;; ends
