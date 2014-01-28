(ns content.cubegeo
  (:require
    [render.rendertarget         :as rt ]
    [cloj.jsutil              :as jsu]
    [render.renderable        :as r]   
    [gaz.layer                :as layer ]
    [content.effect           :as effect]
    [gaz.layerproto           :as layerproto]
    [gaz.three                :as three]   )

  (:require-macros
    [render.rendertarget      :refer [with-rt]]
    [gaz.macros               :refer [with-scene ]])
  )

(def cos (.cos js/Math))
(def sin (.sin js/Math))

(defrecord CubeGeo [layer opts render-target obj posgen]
  effect/Effect
  (update [_ [dx tm]]
    (let [dx (* (aget opts "time_scale"))]
      (doseq [c obj]
        (three/set-pos! c (posgen tm)))
      ))

  (get-output [_]
    (:render-target render-target))

  r/RenderableProto
  (render [_]
    (with-rt (:render-target render-target)
             (r/render layer))))

(defn cos [v] (.cos js/Math v))
(defn sin [v] (.sin js/Math v))

(defn get-pos [t [x y z] [xs ys zs] ts]
  (let [nt (* ts t)
        nx (* xs  (cos nt))
        ny (* ys (sin nt))
        nz (* zs (sin nt))
        ret (array (+ x nx) (+ y ny) (+ z nz))]
    ret
    ))

(defn mk-cube []
  (js/THREE.Mesh.
    (js/THREE.CubeGeometry. 2 2 1 1)
    (js/THREE.MeshPhongMaterial. (js-obj "shininess" 100
                                         "color" 0xff0000))))
(defn mk-cubes [num-of-cubes posgen]
  (for [i (range num-of-cubes) :let [cube (mk-cube)]]
                 (do
                   (three/set-pos! cube (posgen  i))
                   cube)))

(defn mk-layer []
  (layer/mk-perspective-layer 
                 1024 1024 25 (array 0 0 100)
                 {:name "geo" :clear true :clear-color 0x0000ff}))

(defn mk-cube-geo-with-posgen [posgen]
  (let [r-target (rt/mk-render-target 1024 1024)
        layer    (mk-layer)
        opts     (js-obj "time_scale" 1)
        objs     (mk-cubes 10 posgen)]

    (doseq [c objs]
      (layerproto/add layer c))

    (CubeGeo. layer opts r-target objs posgen)))

(defn mk-cube-geo []
  (mk-cube-geo-with-posgen
    #(get-pos % (array 0 0 0) (array 10 15 11 ) 0.001)))

