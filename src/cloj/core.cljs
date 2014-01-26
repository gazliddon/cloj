(ns cloj.core

  (:require

    [gaz.lines                :as lines]

    [goog.dom                 :as dom]
    [cljs.core.async          :as ca :refer [chan <! >! put!]]
    [gaz.system               :as sys]
    [cloj.jsutil              :as jsu]
    [cloj.timechan            :refer [mk-time-chan]]
    [gaz.feedback             :as fb :refer [mk-feedback]]

    [gaz.renderable           :refer [render
                                      RenderableProto
                                      set-renderer!
                                      get-renderer]]

    [gaz.layer                :as layer ]
    [gaz.layerproto           :refer [LayerProto get-scene get-cam]]

    [gaz.rendertarget         :as rt :refer [RenderTarget
                                             mk-render-target
                                             get-current-render-target
                                             get-render-target]]

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
      (doto renderer
        (aset "autoClear" false)
        (aset "autoClearStencil" true)
        (aset "autoClearDepth" true)
        (aset "autoClearColor" true)
        (aset "sortObjects" false)
        (.setSize width height))

      (.appendChild js/document.body (.-domElement renderer))
      {:renderer renderer :width width :height height })))

(defn mk-light []
  (let [light  (js/THREE.DirectionalLight. 0xffffff)
        dir    (.normalize (js/THREE.Vector3. 1 1 1))]
    (set! (.-position light) dir)
    light))

(def test-lines [[0xff0000 [[0 0 0]
                            [0 10 0] ]]

                 [0x00ff00 [[0 0 0]
                            [0 -10 0] ]]

                 [0x0000ff [[0 -10 0]
                            [0 3 0] ]] ])



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; An off screen layer
(defrecord OffscreenLayer
  [layer render-targert material]

  RenderableProto
  (render [_]
    (with-rt (:render-target render-targert)
               (render layer)))
  LayerProto
    (get-scene [_] (:scene layer))
    (get-cam [_] (:cam layer))
    (add [_ obj] (.add (get-scene layer) obj)))

(defn mk-offscreen-layer [width height fov pos]

  (let [render-target (mk-render-target width height)

        material      (js/THREE.MeshPhongMaterial.
                        (js-obj "color" 0xffffffff
                                "shininess" 100
                                "map" (get-render-target render-target)))

        layer         (layer/mk-perspective-layer
                        width height fov pos {:clear false :clear-color 0xff0000})]

    (OffscreenLayer. layer render-target material)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-gui [gui material uni]
  (let [prop (jsu/get-prop material "uniforms" uni)]
    (.add gui prop "value" -10 10)))

(defn game-start []
  (let [ch (mk-time-chan)]
    (math/init! cljs-math)
    (comment listen/on-keys scr got-key!)

    (let [{:keys [width height renderer]} (mk-full-scr-renderer)
          [os-width os-height]  [1024 1024]
          fb (mk-feedback 1024 1024)
          gui (js/dat.GUI.)
          game-layer            (layer/mk-perspective-layer 
                                  width height 25 (array 0 0 18)
                                  {:name "game" :clear true :clear-color 0x0000ff})
          off-scr-layer         (mk-offscreen-layer os-width os-height 45 (array 0 0 100))

          fb-mat (js/THREE.MeshPhongMaterial. (js-obj
                                                "color" 0xffffff
                                                "shininess" 100
                                                "map" (fb/get-buffer fb)))

          plane (js/THREE.Mesh.
                  (js/THREE.PlaneGeometry. 7 7 1 1)
                  fb-mat)

          ]

      (add-gui gui (:material fb) "u_x_scale")
      (add-gui gui (:material fb) "u_y_scale")
      (add-gui gui (:material fb) "u_lpix_scale")
      (add-gui gui (:material fb) "u_mix")
      (add-gui gui (:material fb) "u_fpix_scale")

      (set-renderer! renderer)

      (with-scene (get-scene game-layer)
                  (add (js/THREE.AmbientLight. 0x808080))
                  (add (mk-light))
                  (add plane)
                  (dotimes [_ 0]
                    (obj/add-obj! (mk-one-cube (:material off-scr-layer)))
                    ))

      (with-scene (get-scene off-scr-layer)
                  (add (js/THREE.AmbientLight. 0x202020))
                  (add (mk-light))
                  (dotimes [_ 100]
                    (add-rnd-cube-obj!)) )



      (go-loop [tm 0
                fb fb ]

               (obj/update-objs! tm)

               (render game-layer)
               (render off-scr-layer)

               (fb/render-layer fb (:layer off-scr-layer))
               (aset (jsu/get-prop plane "material") "map" (fb/get-buffer fb))

               (recur
                 (<! ch)
                 (render fb)
                 )))))

(game-start)


