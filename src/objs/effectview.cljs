(ns objs.effectview
  (:require-macros
    [cljs.core.async.macros     :refer [go go-loop]]
    [lt.macros                  :refer [behavior]])
  
  (:require
    [cloj.jsutil          :as jsu :refer [log strlog]]
    [lt.object            :as object]
    [math.vec3            :as v3]
    [objs.cam             :as cam]))

;; need a scene
(defn mk-cube []
  (js/THREE.Mesh.
    (js/THREE.CubeGeometry. 2 2 1 1)
    (js/THREE.MeshPhongMaterial. (js-obj "shininess" 100
                                         "color" 0xffffff))))
(defn mk-cubes [n]
  (for [i (range n)]
    (mk-cube)))

(defn mk-light []
  (let [light  (js/THREE.DirectionalLight. 0xffffff)
        dir    (.normalize (js/THREE.Vector3. 1 1 1))]
    (three/setpos! light dir)))

(behavior ::init!
          :trigger #{:init!}
          :reaction (fn [this t]
                      (let [num-cubes 100
                            scene (js/THREE.Scene.)
                            cs    (mk-cubes num-cubes)]
                        (object/merge! this {:scene js/THREE.Scene.
                                             :cubes mk-cubes
                                             :start-time t
                                             :time-scale 1.0})
                        (doseq [cube cubes] (.add scene cube))
                        (.add scene (mk-light))
                        (object/raise this :update! t))
                      ))

(behavior ::update
          :trigger #{:update!}
          :reaction (fn [this tm]
                      (let [cur @this
                            {:keys [start-time time-scale cubes num-cubes] } cur
                            tm (- tm start-time)]
                        (loop [crest cubes i 0]
                          (when (seq c)
                            (let [c (first crest)]
                              ))
                          (recur (rest crest) (inc i)))
                        )))

(behavior ::render
          :trigger #{:render}
          :reaction (fn [this opts renderer cam rt [t dt]]
                      (let [scene (:scene @this)]
                        (doto renderer
                          (.setClearColor (:clear-color opts) (:alpha opts))
                          (.render scene cam rt (:clear opts))
                          ))
                      ))

