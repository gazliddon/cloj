(ns content.cubegeo
  (:require
    [gaz.math                 :as m]
    [render.rendertarget      :as rt ]
    [cloj.jsutil              :as jsu]
    [render.renderable        :as r]   
    [gaz.layer                :as layer ]
    [content.effect           :as effect]
    [ui.editable              :as editable]
    [ui.attrs                 :as attrs :refer [v3 fl]]
    [gaz.layerproto           :as layerproto]
    [cljs.core.async          :as ca :refer [chan <! >! put!]]
    [gaz.three                :as three]   )

  (:require-macros
    [render.rendertarget      :refer [with-rt]]
    [cljs.core.async.macros   :refer [go ]]
    [gaz.macros               :refer [with-scene ]])
  )

; TODO Put into jsu utils
(def cos (.cos js/Math))
(def sin (.sin js/Math))

; (extend-type js/dat.GUI
;   Object

;   (add3jsCol [gui col]
;     (jsu/log "adding color" col))
;   )

(defn cos [v] (.cos js/Math v))
(defn sin [v] (.sin js/Math v))

(defn get-pos [t [x y z] [xs ys zs] [xt yt zt] ts]
  (let [nt (* ts t)
        nx (* xs  (cos (* nt xt)))
        ny (* ys (sin (* nt yt)))
        nz (* zs (sin (* nt zt)))
        ret (array (+ x nx) (+ y ny) (+ z nz))]
    ret))

; (defn cubes [[tm dx] opts]
;   (let [{:keys [chan rot_scale time-scale pos ts] opts}
;         tm (* tm time-scale) ]
;     (go
;       (let [ [i geo] (<! chan)
;             rot (->>
;                   rot-scale
;                   (m/add-scalar (/ i 30))
;                   (m/mul-scalar (/ 1.0 tm)))
;             pos (get-pos i pos scale time-scale ts) ]
;         (three/set-posrot! geo pos rot)))))

 (def init
 {:rot-scale   (fl 1)
  :time-scale  (v3 [100 200 300] [-200 -200 -200] [200 200 200]) 
  :pos         (v3 ) 
  :ts          (fl 1 -5 5)
  })


(comment defattrs
  (float rot-scale 0 :min 0 :max 10 :name "Rotation Scale")
  (vec3 vec-var (0 0 0) )
  
  )

(defrecord CubeGeo [layer opts render-target obj posgen inputs light ambient attrs]

  editable/UIEditable
  (add-to-dat [this gui]
    (do 
      (attrs/add-attrs-to-gui gui attrs)
      (doto gui
        (.add opts "time_scale" -10 10)
        (.add opts "x_scale" -10 10)
        (.add opts "y_scale" -10 10)
        (.add opts "z_scale" -10 10)
        (.addColor opts "ambient")
        (.addColor opts "light")))
    ; (.add3jsCol gui (aget ambient "color"))
    )

  effect/Effect
  (get-inputs [_]
    inputs)

  (update [_ [dx tm]]
    (let [tm (* tm (aget opts "time_scale"))
          rot-scale (array 200 1000 100)]
      (.set (aget ambient "color") (aget opts "ambient") )
      (.set (aget light "color") (aget opts "light") )
      (loop  [c obj i 0]
        (when (seq c)
          (let [cube (first c)
                rot (->> rot-scale
                      (m/add-scalar (/ i 30))
                      (m/mul-scalar (/ 1.0 tm))) ]

            (three/set-pos! cube (posgen (+ (* i 300.3) tm) opts))
            (three/set-rot! cube rot))

          (recur (rest c) (inc i))))
      ))

  (get-output [_]
    (:render-target render-target))

  r/RenderableProto
  (render [_]
    (with-rt (:render-target render-target)
             (r/render layer))))

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
        light    (mk-light)
        ambient  (js/THREE.AmbientLight. 0x202020)
        layer    (mk-layer)
        opts     (js-obj
                   "time_scale" 1
                   "x_scale" 10
                   "y_scale" 11
                   "z_scale" 11
                   "ambient" "#505050"
                   "light" "#a0a0a0"
                   )
        objs     (mk-cubes opts 20 posgen)]

    (doseq [c objs]
      (layerproto/add layer c))

    (layerproto/add layer ambient)
    (layerproto/add layer light )
    
    (CubeGeo.
      layer opts r-target objs posgen {:tex nil} light ambient init)))

(defn mk-cube-geo []
  (mk-cube-geo-with-posgen
    #(get-pos %1
              (array 0 0 0)
              (array (aget %2 "x_scale") (aget %2 "y_scale") (aget %2 "z_scale"))
              (array 1 2 3) 0.001)))



