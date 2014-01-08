(ns cloj.core
  (:require-macros
    [cljs.core.async.macros   :refer [go go-loop]]
    [gaz.macros               :refer [with-scene]])

  (:require
    [goog.dom                 :as dom]
    [cljs.core.async          :as ca :refer [chan <! >! put!]]
    [gaz.system               :as sys]
    [cloj.jsutil              :as jsu]
    [cloj.timechan            :refer [mk-time-chan]]
    [gaz.feedback             :refer [mk-feedback]]

    [gaz.renderable           :refer [render
                                      RenderableProto
                                      set-renderer!
                                      get-renderer]]

    [gaz.layer                :refer [mk-main-layer LayerProto get-scene]]

    [gaz.rendertarget         :refer [RenderTarget
                                      mk-render-target]]

    [gaz.three                :refer [add set-pos!
                                      rnd-material
                                      set-rot!
                                      set-posrot! ]]
    [gaz.keys                 :as gkeys]
    [gaz.math2                :as math]
    [gaz.rand                 :refer [rnd-v3]]
    [gaz.obj                  :as obj :refer [UpdateObject]]
    [gaz.control              :as control]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn func-on-vals [mp func & kyz]
  (reduce (fn [m v] (assoc m v (func (mp v)))) mp kyz))

(def get-text
  (comp #(.-textContent %) dom/getElement))

(defn get-source [shad]
  (func-on-vals shad get-text "vertexShader" "fragmentShader"))

(def scr (dom/getElement "scr"))

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
  [ cam]
  (comment let [nv (control/keys-to-vel (:vel cam) (gkeys/filter-keys :state)) ]
    (cam/update (assoc cam :vel nv))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def cube-geo (js/THREE.CubeGeometry. 1 1 1 1 1 1))

(defrecord CubeObject [pos vel rot rot-vel msh]
  UpdateObject

  (update [_ tm]

    (let [add-scale #(math/add %1 ( math/mul-scalar 1.0 %2))

          new-vel  (obj/get-oscillate-vel pos vel math/zero 0.0005)
          new-rot  (add-scale rot rot-vel )
          new-pos  (add-scale pos new-vel) ]

      (set-posrot! msh pos rot)
      (CubeObject. new-pos new-vel new-rot rot-vel msh))
    )

  (is-dead? [_] false))

(defn mk-cube [pos material]
  (let [msh (js/THREE.Mesh. (js/THREE.CubeGeometry. 1 1 1 1 1 1) material )]
    (set-pos! msh pos)
    msh))

(defn mk-cube-object [material pos vel rot rot-vel ]
  (let [cube (mk-cube pos material)]
    (add cube)
    (CubeObject. pos vel rot rot-vel cube)))

(defn mk-random-cube-object []
  (mk-cube-object
   (rnd-material)
    (rnd-v3 [-8 -8 -2] [8 8 2])
    (rnd-v3 [-0.05 -0.05 0.5] [0.05 0.05 -0.5])
    (rnd-v3 [-180 -180 -180] [180 180 180])
    (rnd-v3 [-0.05 -0.05 -0.05] [0.005 0.005 0.005])))

(defn add-rnd-cube-obj! []
  (obj/add-obj! (mk-random-cube-object)))

(defn mk-one-cube [material]
  (mk-cube-object
    material
    (rnd-v3 [-2 -2 -2] [2 2 2])
    math/zero
    (math/mk-vec 0.2 0.4 0.4)
    (math/mk-vec 0.01 0.01 -0.003)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; start the app

(def cljs-math { :abs  Math/abs
                 :sqrt Math/sqrt })

(defn mk-full-scr-renderer []
  (let [render-opts {"antialias" false
                     "alpha"     false
                     "stencil"   false }

        renderer (js/THREE.WebGLRenderer. (clj->js render-opts))
        width    (.-innerWidth js/window)
        height   (.-innerHeight js/window) ]
    (do
      (reset! (.-autoClear renderer) false)
      (.setSize renderer width height)
      (.appendChild js/document.body (.-domElement renderer))
      {:renderer renderer :width width :height height })))

(defn mk-light []
  (let [light  (js/THREE.DirectionalLight. 0xffffff)
        dir    (.normalize (js/THREE.Vector3. 1 1 1))]
    (set! (.-position light) dir)
    light))


(defn game-start []
  (let [ch (mk-time-chan)]

    (math/init! cljs-math)
    (comment listen/on-keys scr got-key!)



    (let [{:keys [width height renderer]} (mk-full-scr-renderer)
          game-layer  (mk-main-layer width height)
          off-scr     (mk-render-target 512 512) ]

      (set-renderer! renderer)
      (jsu/log (get-renderer))

      (with-scene (get-scene game-layer)
                  (add (js/THREE.AmbientLight. 0x808080))
                  (add (mk-light))
                  (dotimes [_ 3]
                    (obj/add-obj! (mk-one-cube (:material off-scr)))))

      (with-scene (get-scene off-scr)
                  (add (js/THREE.AmbientLight. 0x202020))
                  (add (mk-light))
                  (dotimes [n 300]
                    (add-rnd-cube-obj!)) )

      (go (while true
            (let [tm (<! ch)]

              (obj/update-objs! tm)

              (render off-scr)
              (render game-layer)

              ))))))

(game-start)


