(ns cloj.core
  (:require-macros
    [cljs.core.async.macros  :refer [go go-loop]]
    [gaz.macros              :refer [with-scene]])

  (:require
    [goog.dom          :as dom]
    [cljs.core.async   :as ca :refer [chan <! >! put!]]
    [gaz.system        :as sys]
    [cloj.jsutil       :as jsu]
    [cloj.timechan     :refer  [mk-time-chan]]
    [gaz.world         :as world]
    [gaz.feedback      :as fb]

    [gaz.renderable    :refer [render RenderableProto]]
    [gaz.layer         :refer [mk-main-layer LayerProto get-scene]]

    [gaz.three         :refer [add set-pos! rnd-material set-rot! set-posrot! ]]
    [gaz.keys          :as gkeys]
    [gaz.cam           :as cam]
    [gaz.math2         :as math]
    [gaz.obj           :as obj :refer [UpdateObject]]
    [gaz.control       :as control]))

(def THREE js/THREE)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn func-on-vals [mp func & kyz]
  (reduce (fn [m v] (assoc m v (func (mp v)))) mp kyz))

(def get-text
  (comp #(.-textContent %) dom/getElement))

(defn get-source [shad]
  (func-on-vals shad get-text :vertexShader :fragmentShader))

(def scr (dom/getElement "scr"))

(def shader-mat-base {:vertexShader "vertexShader"
                      :fragmentShader "fragment_shader_screen"
                      :uniforms {:prevScreen
                                 {:type "t" :value nil} }})
(defn mk-shader-mat [src-texture]
  (let [hsh (assoc
              shader-mat-base
              :uniforms {:prevScreen
                        { :type "t" :value src-texture}}) ]

    (THREE.ShaderMaterial. (clj->js (get-source hsh)))))

(defn mk-shader-mat [src-texture]
  (js/THREE.MeshPhongMaterial. 
    (clj->js {:color 0xffffffff :shininess 100 :map src-texture})) )

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
  (apply math/mk-vec (rnd-vec mn mx [0 1 2])))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def cube-geo (THREE.CubeGeometry. 1 1 1 1 1 1))

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

  (is-dead? [_] false)
  )

(defn mk-cube [pos material]
  (let [msh (THREE.Mesh. (THREE.CubeGeometry. 1 1 1 1 1 1) material )]
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
    (math/mk-vec 0.01 0.01 -0.003)
    ))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; start the app

(def cljs-math { :abs  Math/abs
                 :sqrt Math/sqrt })

(def test-shader { :vertexShader    "vertexShader"
                  :fragmentShader  "fragment_shader_screen"
                  :attributes      {} })

(defn mk-full-scr-renderer []
  (let [renderer-opts {:antialias true
                       :alpha    true 
                       :stencil   false
                       }
        renderer (THREE.WebGLRenderer. (clj->js renderer-opts))
        width    (.-innerWidth js/window)
        height   (.-innerHeight js/window) ]
    (do
      (reset! (.-autoClear renderer) false)
      (.setSize renderer width height)
      (.appendChild js/document.body (.-domElement renderer))
      {:renderer renderer :width width :height height })))

(defn mk-light []
  (let [light  (THREE.DirectionalLight. 0xffffff)
        dir    (.normalize (THREE.Vector3. 1 1 1))]
    (set! (.-position light) dir)
    light))

(defn create-scene []
  (do
    (add (THREE.AmbientLight. 0x808080))
    (add (mk-light))
    (dotimes [n 300]
      (add-rnd-cube-obj!))) )

(defn create-solo-scene [material]
  (do
    (add (THREE.AmbientLight. 0x202020))
    (add (mk-light))
    (obj/add-obj! (mk-one-cube material))
    ))

(defrecord RenderTarget [renderer scene cam render-target material]
  LayerProto
  (get-scene [_] scene)
  
  RenderableProto
  (render [this]
    (.render renderer scene cam render-target)))

(defn mk-render-target [renderer width height]
  (let [rt-opts {:format THREE.RGBFormat
                 :stencilBuffer false}
      
        rt-cam   (THREE.PerspectiveCamera. 50 (/ width height) 0.1 1000)
        rt-rt    (THREE.WebGLRenderTarget. width height (clj->js rt-opts))
        rt-mat   (mk-shader-mat rt-rt) ]

    (set-pos! rt-cam (math/mk-vec 0 0 30))
    (RenderTarget. renderer (THREE.Scene.) rt-cam rt-rt rt-mat)) )

(defn game-start []
  (let [ch (mk-time-chan)]

    (math/init! cljs-math)
    (comment listen/on-keys scr got-key!)

    (let [{:keys [width height renderer]} (mk-full-scr-renderer)
          game-layer (mk-main-layer renderer width height)
          off-scr (mk-render-target renderer 1024 1024) ]

      (with-scene (get-scene game-layer)
                  (create-solo-scene (:material off-scr) )
                  (dotimes [_ 40] 
                    (obj/add-obj! (mk-one-cube (:material off-scr))))
                  )

      (with-scene (get-scene off-scr) 
                   (create-scene))
      (go (while true
            (let [tm (<! ch)]

              (obj/update-objs! tm)

              (render off-scr)
              (render game-layer)

              ))))))

(game-start)


