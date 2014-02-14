(ns objs.cubegeo
  (:require-macros
    [cljs.core.async.macros     :refer [go go-loop]]
    [lt.macros                  :refer [behavior]])
  
  (:require
    [gaz.three                :as three]    
    [cloj.jsutil          :as jsu :refer [log strlog]]
    [ui.attrs                 :as attrs :refer [v3 fl]]
    [lt.object            :as object]
    [math.vec3            :as v3]
    [math.constants  :refer [pi tau]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generic
(defn iterate-items [items fun]
  (let [step (/ 1.0 (count items))]
    (loop [r-items items i 0]
      (when (seq r-items)
        (fun (first r-items) i (* i step))
        (recur (rest r-items) (inc i))))) )

(defn attrs-to-map [attrs mp]
  (reduce (fn [r [k v]] (assoc r k @(:value v))) {}))

(defn cos [v] (.cos js/Math v))
(defn sin [v] (.sin js/Math v))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generic
(behavior ::init!
          :trigger #{:init!}
          :reaction (fn [this scene geo-rec]
                      (object/merge! this {:geo-rec geo-rec
                                           :attrs (:attrs geo-rec)
                                           :objs ((:mk-geo geo-rec )) }
                        )(object/raise this :add scene)))

(behavior ::add-to-scene
          :triger #{:add}
          :reaction (fn [this scene]
                      (let [cur @this]
                        (doseq [c (:objs @this)]
                          (.add scene c)))
                      ))

(behavior ::remove-from-scene
          :trigger #{:remove}
          :reaction (fn [this scene]
                      (assert false)
                      ))

(behavior ::update
          :trigger #{:update}
          :reaction (fn [this tm]
                      (let [{:keys    [attrs objs geo-rec]} @this
                            update-fn (:update geo-rec)]
                        (update-fn objs attrs tm ))))

(object/object* ::geo
                :geo-rec nil
                :attrs nil
                :objs [])

(defn mk-geo [scene geo-rec]
  (let [o (object/create  ::geo)]
    (object/raise o :init! geo-rec)
    o))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Cubey
(defn get-pos [i t [x y z] [xs ys zs] [xt yt zt] ts]
  (let [
        i (* i  tau)
        t (+ (cos i)  t)
        
        nt (* ts t)
        nx (* xs  (cos (* nt xt)))
        ny (* ys (sin (* nt yt)))
        nz (* zs (sin (* nt zt)))
        ret (array (+ x nx) (+ y ny) (+ z nz))]
    ret))

(defn mk-cubes [n]
  (for [i (range n)]
    (js/THREE.Mesh.
      (js/THREE.CubeGeometry. 2 2 1 1)
      (js/THREE.MeshPhongMaterial. (js-obj "shininess" 100
                                           "color" 0xffffff)))))
(defn mk-cube-update-fn [attrs tm]
  (let [mp (attrs-to-map attrs {})
        {:keys [ pos scale v3-time-scale time-scale]} mp ]
    (fn [cube i zto1]
      (three/setpos!  cube (get-pos i zto1 pos scale v3-time-scale time-scale)))))

(defn mk-cube-geo []
  {:attrs {:time-scale    (fl 1.5)
           :rot-scale     (v3 [1 1 1] [-200 -200 -200] [200 200 200]) 
           :pos           (v3 [0 0 28] [-200 -200 -200] [200 200 200])
           :scale         (v3 [12 12 12])
           :v3-time-scale (v3 [4.1 1.3 1]) }

   :mk-geo (fn []
             (mk-cubes 100))

   :update (fn [items attrs tm]
            (iterate-items items (mk-cube-update-fn attrs tm)))})
(defn mk-geo-cube [scene]
  (mk-geo scene (mk-cube-geo)))
