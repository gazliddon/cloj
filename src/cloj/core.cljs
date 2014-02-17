(ns cloj.core

  (:require
    [goog.dom                 :as dom]
    [cljs.core.async          :as ca :refer [chan <! >! put! close!]]

    [content.basicshader      :as basic-shader]
    [content.effect           :as effect] 
    [content.cubegeo          :as cubegeo]

    [ui.editable              :as editable] 

    [cloj.jsutil              :as jsu :refer [log]]
    [cloj.timechan            :refer [mk-time-chan]]

    [render.feedback          :as fb ]
    [render.rendertarget      :as rt ]
    [render.renderable        :as renderable ]

    [gaz.layer                :as layer ]
    [gaz.layerproto           :refer [LayerProto get-scene get-cam]]

    [gaz.three                :refer [add set-pos!
                                      rnd-material
                                      set-rot!
                                      set-posrot! ]]

    [gaz.keys                 :as gkeys]

    [math.vec3                :as v3]
    [math.rand                :refer [rnd-v3]]
    [gaz.obj                  :as obj ]
    [gaz.control              :as control]

    [gaz.listen               :as listen]

    [lt.object                  :as object :refer [raise create object* merge!]]

    [objs.booter :as booter]
    [objs.cubegeo :as cubegeo2]
    )
  (:require-macros
    [cljs.core.async.macros   :refer [go go-loop]]
    [lt.macros                  :refer [behavior]]
    [render.rendertarget      :refer [with-rt]]
    [gaz.macros               :refer [with-scene aloop]])
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
  obj/UpdateObject
  (update [_ tm]

    )

  (is-dead? [_] false)
  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def cube-geo (js/THREE.CubeGeometry. 1 1 1 1 1 1))

(defrecord CubeObject [pos vel rot rot-vel msh]
  obj/UpdateObject

  (update [_ tm]
    (let [scaled-rot-vel (v3/mul-scalar tm rot-vel)
          new-vel  (obj/get-oscillate-vel pos vel v3/zero 0.0005)
          scaled-new-vel (v3/mul-scalar tm new-vel)  ]

      (v3/add! pos scaled-new-vel)
      (v3/add! rot scaled-rot-vel)
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
    (v3/mul-scalar! 0.01 (:pos cb) )
    (v3/mul-scalar! 0.1 (:rot-vel cb))
    cb) )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; start the app

(defn mk-full-scr-renderer []
  (let [render-opts (js-obj
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; An off screen layer
(defrecord OffscreenLayer
  [layer render-targert material ambient-light]

  renderable/RenderableProto
  (render [_]
    (with-rt (:render-target render-targert)
             (renderable/render layer)))
  LayerProto
  (get-scene [_] (:scene layer))
  (get-cam [_] (:cam layer))
  (add [_ obj] (.add (get-scene layer) obj)))

(defn mk-offscreen-layer [width height fov pos]

  (let [render-target (rt/mk-render-target width height)

        material      (js/THREE.MeshPhongMaterial.
                        (js-obj "color" 0xffffffff
                                "shininess" 100
                                "map" (rt/get-render-target render-target)))

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
      (add (mk-light)))
    game-layer))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn mk-feedback-plane [mp sx sy]
  (let [fb-mat (js/THREE.MeshPhongMaterial.
                 (js-obj
                   "color" 0xffffff
                   "shininess" 100
                   "map" mp
                   "transparent" true))
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
  (let [ch (mk-time-chan)
        app (object/create ::mainapp)]

    (go-loop [tick 0]

             (let [tm (<! ch)]
               (object/raise app :update! tm)
               (object/raise app :render))

             (recur (inc tick)))
    ))

(defn add-mainapp-vars! [this]
  (let [{:keys [width height renderer]} (mk-full-scr-renderer)
        [osw osh]       [1024 1024]
        fb              (fb/mk-feedback osw osh basic-shader/basic-shader)
        game-layer      (mk-game-layer width height) 
        off-scr-layer   (mk-offscreen-layer osw osh 45 (array 0 0 100))
        plane           (mk-feedback-plane (fb/get-buffer fb) 10 10)
        opts            (js-obj "time-scale" 1.0 )
        cube-geo        (cubegeo/mk-cube-geo)
        cube-plane      (mk-effect-quad cube-geo (array 0 0 1))
        cube-geo-2      (cubegeo2/mk-cube-geo (get-scene off-scr-layer) 80)]

    (renderable/set-renderer! renderer)
    (with-scene (get-scene game-layer)
                (add cube-plane)
                (add plane))

    (object/merge! this {:osw osw
                         :osh osh
                         :fb fb
                         :game-layer game-layer
                         :off-scr-layer off-scr-layer
                         :plane plane
                         :opts opts
                         :cube-geo cube-geo
                         :cube-plane cube-plane
                         :cube-geo-2 cube-geo-2 })))

(defn main-app-update! [this tm]
  (let [{:keys [cube-geo cube-geo-2]} @this ]
    (raise cube-geo-2 :update! tm)
    (effect/update cube-geo tm)))

(defn main-app-render [this]
  (let [cur @this
        {:keys [cube-geo
                game-layer
                fb
                plane
                off-scr-layer
                ]} cur
        fb-next (fb/flip fb)]

    (renderable/render cube-geo)
    (renderable/render game-layer)

    (fb/render-layer fb (:layer off-scr-layer))
    (set-map! plane (fb/get-buffer fb))
    (renderable/render fb)

    (merge! this {:fb fb-next}))
  )

(object* ::mainapp
                :tags #{:mainapp}
                :init (fn [this]
                        (add-mainapp-vars! this)
                        ))
(behavior ::update!
          :triggers #{:update!}
          :reaction (fn [this tm]
                      (main-app-update! this tm)))

(behavior ::render
          :triggers #{:render}
          :reaction (fn [this]
                      (main-app-render this)))

(do
  ;; Bodge in a function to call after boot
  (merge! booter/booter {:post-boot-fn game-start})
  (raise booter/booter :boot))
  
