(ns cloj.core

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

    [gaz.layer                :refer [mk-perspective-layer ]]
    [gaz.layerproto           :refer [get-scene]]

    [gaz.rendertarget         :as rt
     :refer [RenderTarget
             mk-render-target
             get-current-render-target]]

    [gaz.three                :refer [add set-pos!
                                      rnd-material
                                      set-rot!
                                      set-posrot! ]]
    [gaz.keys                 :as gkeys]
    [gaz.math                 :as math]
    [gaz.rand                 :refer [rnd-v3]]
    [gaz.obj                  :as obj :refer [UpdateObject]]
    [gaz.control              :as control])
  (:require-macros
    [cljs.core.async.macros   :refer [go go-loop]]
    [gaz.rendertarget         :refer [with-rt]]
    [gaz.macros               :refer [with-scene ]])
  )

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

    (let [scaled-rot-vel (math/mul-scalar 1.0 rot-vel)
          new-vel  (obj/get-oscillate-vel pos vel math/zero 0.0005)
          scaled-new-vel (math/mul-scalar 1.0 new-vel)  ]

      (math/add! pos scaled-new-vel)
      (math/add! rot scaled-rot-vel)
      (set-posrot! msh pos rot)
      (CubeObject. pos new-vel rot rot-vel msh))
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
  (let [cb (mk-random-cube-object)
        msh (:msh cb)]
    (aset (:msh cb) "material" material)
    (math/mul-scalar! 0.01 (:pos cb) )
    (math/mul-scalar! 0.1 (:rot-vel cb))
    cb) )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; start the app

(def cljs-math { :abs  Math/abs
                 :sqrt Math/sqrt })

(defn mk-full-scr-renderer []
  (let [
        render-opts (js-obj
                      "antialias" false
                      "alpha" false
                      "stencil" false)

        renderer (js/THREE.WebGLRenderer. render-opts)
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

(defn test-with-rt []
  (do
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    (jsu/log "Current render target")
    (jsu/log (get-current-render-target))


    (jsu/log "About to to with-rt")
    (with-rt "hello"
             (jsu/log "with rt")
             (jsu/log (get-current-render-target))
             )

    (jsu/log "out theother side"
             )

    (jsu/log (get-current-render-target))
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ))

(defn game-start []
  (let [ch (mk-time-chan)]

    (math/init! cljs-math)
    (comment listen/on-keys scr got-key!)

    (let [{:keys [width height renderer]} (mk-full-scr-renderer)
          [os-width os-height]  [1024 1024]
          game-layer            (mk-perspective-layer width height 25 (array 0 0 8))
          off-scr               (mk-render-target os-width os-height)
          off-scr-layer         (mk-perspective-layer os-width os-height 45 (array 0 0 29))]

      (set-renderer! renderer)

      (with-scene (get-scene game-layer)
                  (add (js/THREE.AmbientLight. 0x808080))
                  (add (mk-light))
                  (dotimes [_ 1]
                    (obj/add-obj! (mk-one-cube (:material off-scr)))))

      (with-scene (get-scene off-scr-layer)
                  (add (js/THREE.AmbientLight. 0x202020))
                  (add (mk-light))
                  (dotimes [n 300]
                    (add-rnd-cube-obj!)) )

      (go (while true
            (let [tm (<! ch)]

              (obj/update-objs! tm)

              (with-rt (:render-target off-scr )
                       (render off-scr-layer))

              (with-rt nil 
                       (render game-layer))

              ))))))

(game-start)


