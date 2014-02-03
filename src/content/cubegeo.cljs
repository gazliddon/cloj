(ns content.cubegeo
  (:require
    [math.vec3                 :as m]
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
(defn cos [v] (.cos js/Math v))
(defn sin [v] (.sin js/Math v))

(defn table-lerp [lerp tab i]
  (let [n (count tab)
        i1 (int (mod i n))
        i2 (int (mod (inc i) n))
        frc (- (mod i n) (nth tab i2))]
    (lerp (nth tab i1) (nth tab i2) frc)))

(defn num-lerp [v1 v2 frc]
  (+ v1 (* frc (- v2 v1))))

(defn get-pos [i t [x y z] [xs ys zs] [xt yt zt] ts]
  (let [
        i (* i  6.281592)
        t (+ (cos i)  t)
        
        nt (* ts t)
        nx (* xs  (cos (* nt xt)))
        ny (* ys (sin (* nt yt)))
        nz (* zs (sin (* nt zt)))
        ret (array (+ x nx) (+ y ny) (+ z nz))]
    ret))

(defrecord CubeGeo [layer render-target obj light ambient attrs getter ]

  editable/UIEditable
  (add-to-dat [this gui]
    (do 
      (doto gui
        (attrs/add-attrs-to-gui attrs)
        )))

  effect/Effect
  (get-inputs [_]
    )

  (update [_ [dx tm]]
    (let [tm (/ tm 1000)
          time-scale  (getter :time-scale)
          rot-scale   (getter :scale)
          pos (getter :pos)
          v3-time-scale (getter :v3-time-scale)
          scale (getter :scale)
          step (/ 1.0 (count obj))]
      (loop  [c obj i 0]
        (when (seq c)
          (let [zto1 (* step i)
                cube (first c)
                rot (->> rot-scale
                      (m/add-scalar zto1)
                      (m/mul-scalar tm ))
                pos (get-pos zto1 tm pos scale v3-time-scale time-scale)]
            
            (three/set-posrot! cube pos rot))

          (recur (rest c) (inc i))))
      ))

  (get-output [_]
    (:render-target render-target))

  r/RenderableProto
  (render [_]
    (with-rt (:render-target render-target)
             (r/render layer))))

(defn mk-layer []
  (layer/mk-perspective-layer 
    1024 1024 25 (array 0 0 100)
    {:name "geo" :clear true :clear-color 0x0000ff :alpha 0}))

(defn mk-light []
  (let [light  (js/THREE.DirectionalLight. 0xffffff)
        dir    (.normalize (js/THREE.Vector3. 1 1 1))]
    (set! (.-position light) dir)
    light))

(defn mk-geo [objs init]

  (let [r-target (rt/mk-render-target 1024 1024 {:format js/THREE.RGBAFormat})
        light    (mk-light)
        ambient  (js/THREE.AmbientLight. 0x202020)
        layer    (mk-layer) ]

    (doseq [c objs]
      (layerproto/add layer c))

    (layerproto/add layer ambient)
    (layerproto/add layer light )

    (CubeGeo.
      layer r-target objs light ambient init
      #(attrs/get-attr init %1))))

;; Cube specific stuff

; (defn cubes [[tm dx] ]
;   (let [{:keys [chan rot_scale time-scale pos ts] }
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
  {:time-scale (fl 1.5)
   :rot-scale  (v3 [1 1 1] [-200 -200 -200] [200 200 200]) 
   :pos        (v3 [0 0 28] [-200 -200 -200] [200 200 200])
   :scale (v3 [12 12 12])
   :v3-time-scale (v3 [4.1 1.3 1])
   })

(defn mk-cube []
  (js/THREE.Mesh.
    (js/THREE.CubeGeometry. 2 2 1 1)
    (js/THREE.MeshPhongMaterial. (js-obj "shininess" 100
                                         "color" 0xffffff))))
(defn mk-cubes [num-of-cubes ]
  (for [i (range num-of-cubes) ]
                 ( mk-cube)))

(defn mk-cube-geo []
  (mk-geo (mk-cubes 100) init))

