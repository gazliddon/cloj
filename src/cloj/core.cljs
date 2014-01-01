(ns cloj.core
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [gaz.macros             :refer [with-scene]])

  (:require [goog.dom        :as dom]
            [goog.events     :as events]
            [gaz.feedback    :as fb]
            [cljs.core.async :as ca :refer [chan <! >! put!]]
            [gaz.system      :as sys]
            [cloj.jsutil     :as jsu]
            [gaz.world       :as world]
            
            [gaz.gamescreen  :refer [mk-game-screen get-main-scene]]
            [gaz.renderable  :refer [render]]
            
            [gaz.listen      :as listen]
            [gaz.three       :as three]
            [gaz.keys        :as gkeys]
            [gaz.cam         :as cam]
            [gaz.math        :as math]
            [gaz.obj         :as obj]
            [gaz.control     :as control]))

(def THREE js/THREE)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn func-on-vals [mp func kyz]
  (reduce (fn [m v] (assoc m v (func (mp v)))) mp kyz))

(def get-text
  (comp #(.-textContent %) dom/getElement))

(defn get-source [shad]
  (func-on-vals shad get-text [:vertexShader :fragmentShader]))

(def scr (dom/getElement "scr"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Time test stuff
(defn- req-anim-frame [outchan]
  (do
    (js/requestAnimationFrame #(req-anim-frame outchan))
    (put! outchan (.now js/performance))
    outchan))

(defn mk-time-chan []
  (let [buff (ca/sliding-buffer 1)
        ch (chan buff)
        fun (fn [[a b]] (- b a))]
    (ca/map< fun (ca/partition 2 (req-anim-frame ch)))))

(def time-chan (mk-time-chan))

(defn do-time-stuff [f]
  (go (while true
        (let [v (<! time-chan)]
          (f v)))))

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
  (listen/on-keys elem (comp (partial put! channel) xform-key-event)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn cam-func
  "update the camera from the keys!"
  [^C/Cam cam]
  (let [nv (control/keys-to-vel (:vel cam) (gkeys/filter-keys :state)) ]
    (cam/update (assoc cam :vel nv))))

(defn update-func [cam]
  (do
    (obj/update-objs! 0)
    (cam-func cam)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn rnd-rng [lo hi] (+ lo (rand (- hi lo))))
(defn rnd-norm [] (rnd-rng -0.5 0.5))

(defn rnd-vec [vmin vmax kyz]
  (map #(rnd-rng (vmin %1) (vmax %1)) kyz ))

(defn rnd-v3 [mn mx]
  (math/mk-vec (rnd-vec mn mx [0 1 2])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn copy-field-to-geo-pos! [obj geo-key]
  (three/set-pos! (geo-key obj) (:pos obj))
  obj)

(defn mk-cube [pos]
  (let [geo (THREE.CubeGeometry. 1 1 1 1)
        mat three/g-material
        msh (THREE.Mesh. geo mat)]
    (three/set-pos! msh pos)
    msh)) 

(defn cube-init [obj & rst]
  (let [cube (mk-cube (:pos obj))]
    (three/add cube)
    (assoc obj
           :cube cube
           :rvelx (rnd-rng -0.05 0.05)
           :rvely (rnd-rng -0.05 0.05)
           :rvelz (rnd-rng -0.05 0.05)
           :rot (rnd-v3 [-180 -180 -180] [180 180 180]))))

(defn cube-update! [obj tm]
  (let [cube (:cube obj)]
    (set! (.-x (.-rotation cube))  (+ (:rvelx obj) (.-x (.-rotation cube))))
    (set! (.-y (.-rotation cube))  (+ (:rvely obj) (.-y (.-rotation cube))))
    (set! (.-z (.-rotation cube))  (+ (:rvelz obj) (.-z (.-rotation cube))))
    (-> obj
      (obj/home! math/zero 0.0005)
      (copy-field-to-geo-pos! :cube))))


(def obj-types { :cube {:init   cube-init
                        :update cube-update!}})

(defn add-rnd-cube-obj! []
  (obj/add-obj-from-typ!
    (obj-types :cube)
    (rnd-v3 [-8 -8 -2] [8 8 2])
    (rnd-v3 [-0.05 -0.05 0.5] [0.05 0.05 -0.5])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; start the app

(def cljs-math { :abs  Math/abs
                 :sqrt Math/sqrt })
(def test-shader
                  { :vertexShader    "vertexShader"
                    :fragmentShader  "fragment_shader_screen"
                    :attributes      {}
                    :uniforms        {:time { :type "f" :value 0.0 } }})

(def map-to-shader-material
  (comp three/mk-shader-material get-source))

(defn mk-full-scr-renderer []
  (let [renderer (THREE.WebGLRenderer.)
        width    (.-innerWidth js/window)
        height   (.-innerHeight js/window) ]
    (do
      (.setSize renderer width height)
      (.appendChild js/document.body (.-domElement renderer))
      {:renderer renderer :width width :height height })))

(defn game-start []
  (do
    (math/init! cljs-math)

    (comment listen/on-keys scr got-key!)

    (let [fsrend (mk-full-scr-renderer)
          width  (:width fsrend)
          height (:height fsrend)
          renderer (:renderer fsrend)
          gs     (mk-game-screen width height) ]

      (with-scene (get-main-scene gs)
                  (three/add (THREE.AmbientLight. 0x202020))
                  (dotimes [n 100]
                    (add-rnd-cube-obj!)))

      (go (while true
            (let [tm (<! time-chan)]
              (obj/update-objs! tm)
              (render gs renderer)
              ))))))

(game-start)


