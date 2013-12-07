;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(ns cloj.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :as ca]
            [gaz.system :as sys]
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Take an action on a key
;; Should really just xform it into a platform neutral message and send on
(defn got-key! [e]
  (let [etype  (.-type e)
        keyfun (partial gkeys/new-state! (char (.-keyCode e)))
        reset gkeys/all-reset!]
    (condp = etype
      "keydown"   (keyfun true)
      "keyup"     (keyfun false)
      "focuslost" (reset))))

(defn xform-key-event
  "Transform a web event into a key one"
  [ev]
  (let [etype   (.-type ev)
        keycode (.-keycode ev)]
    (condp = etype
               "keydown"   {:event :down :key keycode}
               "keyup"     {:event :up   :key keycode}
               "focuslost" {:event :focuslost}
               {})))

(defn setup-key-listener [elem channel]
  (listen/on-keys elem (comp (partial ca/put! channel) xform-key-event)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn cam-func
  "update the camera from the keys!"
  [^C/Cam cam]
  (let [nv (control/keys-to-vel (:vel cam) (gkeys/filter-keys :state)) ]
    (cam/update (assoc cam :vel nv))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; start the app

(def cljs-math { :abs  Math/abs
                 :sqrt Math/sqrt })
(do
  (let [sys (sys/init!)]

    (math/init! cljs-math)
    (logit "Here we go")
    (three/init cam-func)
    (listen/on-keys scr got-key!)
    (do-time-stuff (fn [v] (comment logit (str v))))
    (mk-world-geom!)))
