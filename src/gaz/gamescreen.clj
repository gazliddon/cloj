(ns gaz.gamescreen 
  (:require
    [cloj.jsutil    :as jsu]
    [gaz.renderable :refer [RenderableProto render]]
    [gaz.layer      :refer [mk-main-layer mk-hud-layer get-scene]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The game screen
(defrecord GameScreen [main-layer hud-layer layer-list ]

  RenderableProto

  (render [this renderer]
    (doseq [layer layer-list]
      (render layer renderer ))))

(defn mk-game-screen
  [width height]
  (let [main-layer  (mk-main-layer width height)
        hud-layer   (mk-hud-layer  width height)]
    (GameScreen.  main-layer hud-layer [main-layer hud-layer])))

(def get-main-scene (comp get-scene :main-layer))
(def get-hud-scene (comp get-scene :hud-layer))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;ends








