;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(ns cloj.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :as ca]
            [gaz.world :as world]
            [gaz.listen :as listen]
            [gaz.three :as three]
            [gaz.keys :as gkeys]
            [gaz.cam :as cam]
            [gaz.math :as math]
            [gaz.control :as control]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- logit [x](. js/console (log x)))

(def scr (dom/getElement "scr"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Time test stuff
(defn- req-anim-frame [outchan]
  (do
    (js/requestAnimationFrame #(req-anim-frame outchan))
    (ca/put! outchan (.now js/performance))
    outchan))

(defn mk-time-chan []
  (ca/map<
    (fn [[a b]] (- b a))
    (ca/partition 2 (req-anim-frame (ca/chan)))))

(def time-chan (mk-time-chan))

(defn do-time-stuff [f]
  (go (while true
        (let [v (ca/<! time-chan)]
          (f v)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(def mk-cube-from-tile (comp three/add-geom (partial three/mk-cube-mat three/r-material ) :pos))
(defn mk-world-geom! [] (world/iterate-world mk-cube-from-tile world/world-map) )

;; Pipe keys through to control and get a velocity vector
(defn got-key! [e]
  (let [etype  (.-type e)
        keyfun (partial gkeys/new-state! (char (.-keyCode e)))
        reset gkeys/all-reset!]
    (condp = etype
      "keydown"   (keyfun true)
      "keyup"     (keyfun false)
      "focuslost" (reset))))

(defn keys-to-vel
  "return a new velocity adjusted by the controls"  
  [curr-vel curr-keys]
  (->> curr-keys
       (control/keys-to-vel)
       (math/add curr-vel)
       (control/clamp-vel)
       (math/muls 0.95)))

(defn cam-func!
  "update the camera from the keys!"
  [^C/Cam cam]
  (comment let [nv ( keys-to-vel (:vel cam) (gkeys/filter-keys :state))]
    (assoc cam :pos (math/add (:pos cam) nv) :vel nv)) cam)

(defn every-frame! [] (swap! three/cam cam-func!))

(def cljs-math #{ :abs Math/abs
                  :sqrt Math/sqrt
                 })

;; start the app
(do
  (math/init! cljs-math) 
  (logit "Here we go")
  (three/init every-frame!)
  (listen/on-keys scr got-key!)
  (do-time-stuff (fn [v] (logit (str v))))
  (mk-world-geom!))
