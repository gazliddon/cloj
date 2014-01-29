(ns gaz.lines
  (:require
    [cloj.jsutil              :as jsu]
    [gaz.three :refer [add]]
    [gaz.col :as col])
  
  (:require-macros
    [gaz.macros :refer [with-scene]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Misc utils


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Color utils
(defn set-col [col [ r g b]]
  (aset col "r" r )
  (aset col "g" g )
  (aset col "b" b )
  )



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Color utils
(defn- to-three-vec 
  "Convert an array of [x y z] to a js array
  of THREE.Vector3.  "
  [xs]
  (to-array
    (map (fn [[x y z]] (js/THREE.Vector3. x y z)) xs)))

(defn- mk-line
  "make a THREE.Line. object from a seq of [x y z] positions
  and a colour"

  [col xs]

  (let [geo     (js/THREE.Geometry.)
        line    (js/THREE.Line. geo)
        colprop (get-in line [ "material" "color" ]) ]

    (aset geo "vertices" (to-three-vec xs))
    (set-col colprop (col/hex-col-to-vec col))
    line))

(defn lines-to-scene
  "Create a scene full of lines "
  [lines]
  (let [scn (js/THREE.Scene.)]
    (with-scene scn
      (doseq [[col line-seq] lines]
        (add (mk-line col line-seq)))
    scn)))

;; ends
