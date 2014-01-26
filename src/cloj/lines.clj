(ns gaz.lines
  (:require
    [gaz.three :refer [add]]
    [gaz.col :as col]
    [cloj.jsutil as jsu]]
    )
  
  (:require-macros
    [gaz.macros :refer [with-scene]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Color utils
(defn set-col [col [ r g b a]]
  (aset col "r" r )
  (aset col "g" g )
  (aset col "b" b )
  (aset col "a" a ) )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Color utils
(defn- to-three-vec [xs]
  (to-array
    (map #(js/THREE.Vector3. (% 0)(% 1)(% 2)) xs)))

(defn mk-line [col xs]
  (let [line (js/THREE.Line. (to-three-vec xs))
        colprop (jsu/get-prop line "material" "color")]
    (set-col colprop (col/hex-col-to-vec col))
    line))

(defn lines-to-scene [lines]
  (let [scn (js/THREE.Scene.)]
    (with-scene scn
      (doseq [[col line-seq] lines]
        (add (mk-line col line-seq)))
    scn))

