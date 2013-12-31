(ns gaz.game-screen 
  (:require
    [gaz.renderable :refer [RenderableProto]]
    [gaz.layer :refer [mk-main-layer mk-hud-layer]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The game screen
(defrecord GameScreen [renderer main-layer hud-layer layer-list ]

  RenderableProto

  (render [this]
    (doseq [layer layer-list]
      (render layer))))

(defn mk-game-screen
  [width height]
  (let [render      (THREE.WebGLRenderer.)
        main-layer  (mk-main-layer renderer width height)
        hud-layer   (mk-hud-layer  renderer width height)]
    (do
      (.setSize renderer width height)
      (GameScreen.
        main-layer hud-layer [main-layer hud-layer]))))

(def get-main-scene (comp get-scene :main-layer))
(def get-hud-scene (comp get-scene :hud-layer))
(defn get-renderer [gs] (:render gs))

(defn mk-game-screen-in-window [window]
  (let [width  (.-innerWidth window)
        height (.-innerHeight window)
        screen (mk-game-screen width height)]
    (do
      (.appendChild window (.-domElement (get-renderer screen)))
      screen)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;ends








