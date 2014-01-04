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

(defn mk-img-mat [img-uri]
  (js/THREE.MeshLambertMaterial.
    (js-obj
      "map" (.loadTexture js/THREE.ImageUtils img-uri))))

(defn mk-t-quad []
  (js/THREE.Mesh.
      (js/THREE.PlaneGeometry. 10 10)
      (mk-img-mat "feedback.jpg")))

(def shit-mat (mk-img-mat "feedback.jpg"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn mk-three-vec [[x y z]] (js/THREE.Vector3. x y z))

(defn set-rot! [cb [x y z]]
  (set! (.-x (.-rotation cb)) x  )
  (set! (.-y (.-rotation cb)) y  )
  (set! (.-z (.-rotation cb)) z  )  )

(defn set-pos! [cb [x y z]]
  (set! (.-x (.-position cb)) x  )
  (set! (.-y (.-position cb)) y  )
  (set! (.-z (.-position cb)) z  )  )
(defn set-posrot! [msh pos rot]
  (set-pos! msh pos)
  (set-rot! msh rot))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def material
  (js/THREE.MeshPhongMaterial. (clj->js {"color" 0x00ff00 "shininess" 100})))

(def r-material
  (js/THREE.MeshPhongMaterial. (clj->js {"shininess" 100 "color" 0xff0000})))

(def g-material
  (js/THREE.MeshPhongMaterial. (clj->js {"shininess" 100 "color" 0x00ff00})))

(def b-material
  (js/THREE.MeshPhongMaterial. (clj->js {"shininess" 100 "color" 0x0000ff})))

(def all-mats
  [r-material
   g-material
   b-material])

(defn rnd-material []
  (let [n (jsu/random-int 3)]
    (all-mats n)) )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


