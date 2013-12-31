(ns cloj.core
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require [goog.dom        :as dom]
            [goog.events     :as events]
            [gaz.feedback    :as fb]
            [cljs.core.async :as ca]
            [gaz.system      :as sys]
            [cloj.jsutil     :as jsu]
            [gaz.world       :as world]
            
            [cloj.g3d        :as g3d]
            [gaz.render-targets :as rt]

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
    (ca/put! outchan (.now js/performance))
    outchan))

(defn mk-time-chan []
  (ca/map<
    (fn [[a b]] (- b a))
    (ca/partition 2 (req-anim-frame (ca/chan)))))

(comment def time-chan (mk-time-chan))

(comment defn do-time-stuff [f]
  (go (while true
        (let [v (ca/<! time-chan)]
          (f v)))))

(def mk-cube-from-tile
  (comp three/add-geom (partial three/mk-cube-mat three/r-material ) :pos))

(defn mk-world-geom! []
  (world/iterate-world mk-cube-from-tile world/world-map) )

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

(defn update-func [cam]
  (do
    (obj/update-objs!)
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

(defn cube-init [obj & rst]
  (let [cube (three/add-cube (:pos obj))
        ]
    (assoc obj
           :cube cube
           :rot2 (math/mk-vec 0 1 0)
           :rvelx (rnd-rng -0.05 0.05)
           :rvely (rnd-rng -0.05 0.05)
           :rvelz (rnd-rng -0.05 0.05)
           :rot (rnd-v3 [-180 -180 -180] [180 180 180]))))

(defn cube-update! [obj]
  (let [cube (:cube obj)]
    (set! (.-x (.-rotation cube))  (+ (:rvelx obj) (.-x (.-rotation cube))))
    (set! (.-y (.-rotation cube))  (+ (:rvely obj) (.-y (.-rotation cube))))
    (set! (.-z (.-rotation cube))  (+ (:rvelz obj) (.-z (.-rotation cube))))
    (comment three/set-rot! (:cube obj) (:rot2 obj))
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

(def test-data
  {  :render-target   #(fb/mk-render-target 512 512 )
     :test-material   #(map-to-shader-material test-shader)
     :camera          #(fb/mk-cam 512 512)
   })

(defn do-tests [t-data]
  (doseq [[k v] t-data]
    (jsu/log (name k))
    (jsu/log (v))))
(do
  (let [sys 1]
    (math/init! cljs-math)
    (three/init update-func )

    (dotimes
      [n 100] (add-rnd-cube-obj!))
    (listen/on-keys scr got-key!)))






