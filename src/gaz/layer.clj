(ns gaz.layer
  (:require
    [gaz.layerproto :refer [LayerProto]]
    [gaz.rendertarget :as rt]
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
    (let [r       (get-renderer)
          rtarget  rt/*current-rt*] 
      (if (nil? rtarget)
        (.render r scene cam)
        (.render r scene cam rtarget)))))

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

