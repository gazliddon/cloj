(ns objs.cubegeo
  (:require-macros
    [lt.macros                   :refer [behavior]])
  
  (:require
    [cloj.jsutil                 :as jsu :refer [log]]
    [lt.object                   :as object :refer [raise create object* merge!]]
    [objs.geo                    :as geo]
    [gaz.three                   :as three]
    [ui.attrs                    :as attrs :refer [v3 fl]]
    [math.misc                   :as math :refer [cos sin]]
    [math.constants              :refer [pi tau]]))

(defn- get-pos [i t [x y z] [xs ys zs] [xt yt zt] ts]
  (let [i (* i ts)
        t (+ i  t)
        nt (* 1 t)
        nx (* xs (cos (* nt xt)))
        ny (* ys (sin (* nt yt)))
        nz (* zs (sin (* nt zt)))
        ret (array (+ x nx) (+ y ny) (+ z nz))]
    ret))

(defn- mk-cubes [n]
  (for [i (range n)]
    (js/THREE.Mesh.
      (js/THREE.CubeGeometry. 2 2 1 1)
      (three/rnd-material)
      )))

(defn- cube-update [items attrs-map [dt t]]
  (let [{:keys [pos scale v3-time-scale time-scale]} attrs-map ]
    (geo/iterate-items
      items
      (fn [cube i zto1]
        (three/set-pos! cube (get-pos i (/ t 1000) pos scale v3-time-scale time-scale))))))

(defn- mk-cube-attrs []
  {:time-scale    (fl 3)
   :rot-scale     (v3 [1 1 1] [-200 -200 -200] [200 200 200]) 
   :pos           (v3 [0 0 28] [-200 -200 -200] [200 200 200])
   :scale         (v3 [12 12 12])
   :v3-time-scale (v3 [4.1 1.3 1]) })

(defn mk-cube-geo [scene n-cubes]
  (let [cgeo (geo/mk-geo scene (mk-cube-attrs) (mk-cubes n-cubes) cube-update)]
    cgeo) )

