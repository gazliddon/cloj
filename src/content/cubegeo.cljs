(ns content.cubegeo
  (:require
    [render.rendertarget         :as rt ]
    [cloj.jsutil              :as jsu]
    [render.renderable        :as r]   
    [gaz.layer                :as layer ]
    [content.effect           :as effect]
    [ui.editable              :as editable]
    [gaz.layerproto           :as layerproto]
    [gaz.three                :as three]   )

  (:require-macros
    [render.rendertarget      :refer [with-rt]]
    [gaz.macros               :refer [with-scene ]])
  )

(def cos (.cos js/Math))
(def sin (.sin js/Math))


; (extend-type js/dat.GUI
;   Object

;   (add3jsCol [gui col]
;     (jsu/log "adding color" col))
;   )

(defrecord CubeGeo [layer opts render-target obj posgen inputs light ambient]

  editable/UIEditable
  (add-to-dat [this gui]
    (.add gui opts "time_scale" -10 10)
    (.add gui opts "x_scale" -10 10)
    (.add gui opts "y_scale" -10 10)
    (.add gui opts "z_scale" -10 10)
    (.addColor gui opts "ambient")
    (.addColor gui opts "light")
    ; (.add3jsCol gui (aget ambient "color"))
    )

  effect/Effect
  (get-inputs [_]
    inputs)

  (update [_ [dx tm]]
    (let [tm (* tm (aget opts "time_scale"))]
      (.set (aget ambient "color") (aget opts "ambient") )
      (.set (aget light "color") (aget opts "light") )
      (loop  [c obj i 0]
        (when (seq c)
          (let [cube (first c)]
            (three/set-pos! cube (posgen (+ (* i 300.3) tm) opts))

            (aset (get cube :rotation) "y" (+ (/ tm 2000) (/ i 30))) 
            (aset (get cube :rotation) "z" (+ (/ tm 1000) (/ i 30))) 
            (aset (get cube :rotation) "x" (+ (/ tm 1000) (/ i 30))))

          (recur (rest c) (inc i))))
      ))

  (get-output [_]
    (:render-target render-target))

  r/RenderableProto
  (render [_]
    (with-rt (:render-target render-target)
             (r/render layer))))

(defn cos [v] (.cos js/Math v))
(defn sin [v] (.sin js/Math v))

(defn get-pos [t [x y z] [xs ys zs] [xt yt zt] ts]
  (let [nt (* ts t)
        nx (* xs  (cos (* nt xt)))
        ny (* ys (sin (* nt yt)))
        nz (* zs (sin (* nt zt)))
        ret (array (+ x nx) (+ y ny) (+ z nz))]
    ret
    ))

(defn mk-cube []
  (js/THREE.Mesh.
    (js/THREE.CubeGeometry. 2 2 1 1)
    (js/THREE.MeshPhongMaterial. (js-obj "shininess" 100
                                         "color" 0xffffff))))
(defn mk-cubes [opts num-of-cubes posgen]
  (for [i (range num-of-cubes) :let [cube (mk-cube)]]
                 (do
                   (three/set-pos! cube (posgen i opts))
                   cube)))

(defn mk-layer []
  (layer/mk-perspective-layer 
                 1024 1024 25 (array 0 0 100)
                 {:name "geo" :clear true :clear-color 0x0000ff :alpha 0}))

(defn mk-light []
  (let [light  (js/THREE.DirectionalLight. 0xffffff)
        dir    (.normalize (js/THREE.Vector3. 1 1 1))]
    (set! (.-position light) dir)
    light))

(defn mk-cube-geo-with-posgen [posgen]
  (let [r-target (rt/mk-render-target 1024 1024 {:format js/THREE.RGBAFormat})
        light (mk-light)
        ambient (js/THREE.AmbientLight. 0x202020)
        layer    (mk-layer)
        opts     (js-obj
                   "time_scale" 1
                   "x_scale" 10
                   "y_scale" 115
                   "z_scale" 11
                   "ambient" "#505050"
                   "light" "#a0a0a0"
                   )
        objs     (mk-cubes opts 20 posgen)]

    (doseq [c objs]
      (layerproto/add layer c))

    (layerproto/add layer ambient)
    (layerproto/add layer light )
    
    (CubeGeo. layer opts r-target objs posgen {:tex nil} light ambient)))



(defn mk-cube-geo []
  (mk-cube-geo-with-posgen
    #(get-pos %1
              (array 0 0 0)
              (array (aget %2 "x_scale") (aget %2 "y_scale") (aget %2 "z_scale"))
              (array 1 2 3) 0.001)))

(defrecord Tex [name offset scale rot mp])



