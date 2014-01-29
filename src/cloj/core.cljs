(ns cloj.core

  (:require

    [gaz.lines                :as lines]
    [gaz.effects :as effects]
    [ui.editable :as  editable] 

    [goog.dom                 :as dom]
    [cljs.core.async          :as ca :refer [chan <! >! put!]]
    [gaz.system               :as sys]
    [cloj.jsutil              :as jsu]
    [cloj.timechan            :refer [mk-time-chan]]
    [gaz.feedback             :as fb :refer [mk-feedback]]

    [render.renderable        :refer [render
                                      RenderableProto
                                      set-renderer!
                                      get-renderer]]

    [gaz.layer                :as layer ]
    [gaz.layerproto           :refer [LayerProto get-scene get-cam]]

    [render.rendertarget         :as rt :refer [RenderTarget
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
    [gaz.control              :as control]
    
    [content.effect  :as  effect] 
    [content.cubegeo :as cubegeo])

  (:require-macros
    [cljs.core.async.macros   :refer [go go-loop]]
    [render.rendertarget      :refer [with-rt]]
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


(defrecord Cube2Object [pos start-time msh]
  UpdateObject
  (update [_ tm]

    )
  
  (is-dead? [_] false)
  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def cube-geo (js/THREE.CubeGeometry. 1 1 1 1 1 1))

(defrecord CubeObject [pos vel rot rot-vel msh]
  UpdateObject

  (update [_ tm]
    (let [scaled-rot-vel (math/mul-scalar tm rot-vel)
          new-vel  (obj/get-oscillate-vel pos vel math/zero 0.0005)
          scaled-new-vel (math/mul-scalar tm new-vel)  ]

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
                      "alpha" true
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
;; An off screen layer
(defrecord OffscreenLayer
  [layer render-targert material ambient-light]

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
                        width height fov pos {:clear false :clear-color 0xff0000})
        ambient-light (js/THREE.AmbientLight. 0x404040)]

    (with-scene (get-scene layer)
                (add ambient-light)
                (add (mk-light)))

    (OffscreenLayer. layer render-target material ambient-light)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn mk-game-layer [width height]
  (let [game-layer (layer/mk-perspective-layer 
                     width height 25 (array 0 0 18)
                     {:name "game" :clear true :clear-color 0x0000ff})]
    (with-scene
      (get-scene game-layer)
      (add (js/THREE.AmbientLight. 0x808080))
      (add (mk-light))
      )
    game-layer))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn mk-feedback-plane [mp sx sy]
  (let [fb-mat (js/THREE.MeshPhongMaterial.
                 (js-obj
                   "color" 0xffffff
                   "shininess" 100
                   "map" mp
                   "transparent" true
                   ))
        plane (js/THREE.Mesh.
                (js/THREE.PlaneGeometry. sx sy 1 1)
                fb-mat)]

    plane))

(defn set-map! [obj mp] (aset (aget obj "material") "map" mp))

(defn mk-effect-quad [effect pos]
  (let [plane (mk-feedback-plane (effect/get-output effect) 2 2)]
    (set-pos! plane pos)
    plane))

(defn game-start []
  (let [ch (mk-time-chan)]
    (math/init! cljs-math)
    (comment listen/on-keys scr got-key!)

    (let [{:keys [width height renderer]} (mk-full-scr-renderer)
          [osw osh]       [1024 1024]
          fb              (mk-feedback osw osh effects/basic-shader)
          gui             (js/dat.GUI.)
          game-layer      (mk-game-layer width height) 
          off-scr-layer   (mk-offscreen-layer osw osh 45 (array 0 0 100))
          plane           (mk-feedback-plane (fb/get-buffer fb) 10 10)
          opts            (js-obj "time-scale" 1.0 )
          cube-geo        (cubegeo/mk-cube-geo)
          cube-plane      (mk-effect-quad cube-geo (array 0 0 1)) ]

      (editable/add-to-dat fb gui)
      (editable/add-to-dat cube-geo (.addFolder gui "Geometry"))

      (.add gui opts "time-scale" 0.0001 3)

      (set-renderer! renderer)

      (with-scene (get-scene game-layer)
                  (add cube-plane)
                  (add plane))

      (with-scene (get-scene off-scr-layer)
                  (dotimes [_ 100]
                    (add-rnd-cube-obj!)) )

      (go-loop [[dx time] [0 0]
                fb fb ]

               (obj/update-objs! (* (aget opts "time-scale") dx))
               (effect/update cube-geo [dx time])

               (render cube-geo)

               (render game-layer)

               (fb/render-layer fb (:layer off-scr-layer))
               (set-map! plane (fb/get-buffer fb))
               (render fb)

               (recur
                 (<! ch) (fb/flip fb))))))

(game-start)

(defprotocol Editable
  (populate-gui [this gui])
  )



