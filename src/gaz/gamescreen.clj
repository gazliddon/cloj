(ns gaz.gamescreen 
  (:require
    [cloj.jsutil    :as jsu]
    [gaz.renderable :refer [RenderableProto render]]
    [gaz.layer      :refer [mk-main-layer mk-hud-layer get-scene get-cam]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The game screen
(defrecord GameScreen [main-layer hud-layer layer-list ]

  RenderableProto

  (render
    [this] (render main-layer )))

(defn mk-game-screen
  [width height]
  (let [main-layer  (mk-main-layer width height)
        hud-layer   (comment mk-hud-layer width height)]
    (GameScreen.  main-layer hud-layer [main-layer hud-layer])))

(def get-main-scene (comp get-scene :main-layer))
(def get-hud-scene (comp get-scene :hud-layer))

(def get-main-cam (comp get-cam :main-layer))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;ends








