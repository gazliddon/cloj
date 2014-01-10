(ns gaz.three
  (:require-macros [gaz.macros :refer [with-scene]])
  (:require
    [cloj.jsutil :as jsu ]
    ))

;; Current scene stuff
(def ^:dynamic *current-scene* nil)

(defn add [o]
  (.add *current-scene* o))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn mk-shader-material [shad]
  (js/THREE.ShaderMaterial. (jsu/tojs shad)))

(comment defn mk-img-mat [img-uri]
  (js/THREE.MeshLambertMaterial.
    (js-obj
      "map" (.loadTexture js/THREE.ImageUtils img-uri))))

(comment defn mk-t-quad []
  (js/THREE.Mesh.
      (js/THREE.PlaneGeometry. 10 10)
      (mk-img-mat "feedback.jpg")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn mk-three-vec [[x y z]] (js/THREE.Vector3. x y z))

(defn set-rot! [cb a]
  (.fromArray (.-rotation cb) a) )

(defn set-pos! [cb a]
  (.fromArray ( .-position cb) a))

(defn set-posrot! [msh pos rot]
  (set-pos! msh pos)
  (set-rot! msh rot))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- default-mat-settings [col] (js-obj "color" col  "shininess" 100 ))

(def material
  (js/THREE.MeshPhongMaterial. (default-mat-settings 0xffffff)))

(def r-material
  (js/THREE.MeshPhongMaterial. (default-mat-settings 0xff0000)))

(def g-material
  (js/THREE.MeshPhongMaterial. (default-mat-settings 0x00ff00)))

(def b-material
  (js/THREE.MeshPhongMaterial. (default-mat-settings 0x0000ff)))

(def all-mats
  [r-material
   g-material
   b-material])

(defn rnd-material []
  (let [n (jsu/random-int (count all-mats))]
    (all-mats n)) )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


