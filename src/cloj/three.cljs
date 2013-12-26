(ns gaz.three
  (:require
    [cloj.jsutil :as jsu ]
    [gaz.math :as math ]
    [gaz.feedback :as fb ]
    [gaz.cam :as Cam]))

(def THREE js/THREE)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn mk-shader-material [shad]
  (THREE.ShaderMaterial. (jsu/tojs shad)))

(defn mk-img-mat [img-uri]
  (THREE.MeshLambertMaterial.
    (js-obj
      "map" (.loadTexture THREE.ImageUtils img-uri))))

(defn mk-t-quad []
  (THREE.Mesh.
      (THREE.PlaneGeometry. 10 10)
      (mk-img-mat "feedback.jpg")))

(def shit-mat (mk-img-mat "feedback.jpg"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Experimental material stuff

(def materials {:red-flat   {:three-material THREE.MeshPhongMaterial
                             :color 0xff0000
                             :shininess 100 }

                :green-flat {:three-material THREE.MeshPhongMaterial
                             :color 0x00ff00
                             :shininess 100 }

                :blue-flat  {:three-material THREE.MeshPhongMaterial
                             :color 0x0000ff
                             :shininess 100 }

                :feedback-img {:three-material THREE.MeshPhongMaterial
                               :color 0x00ff00
                               :shininess 100
                               :map "feedback.jpg" }})

(def xforms
  {:map #(.loadTexture THREE.ImageUtils %1)})

(defn do-xform [xforms-tab k v]
  (if (xforms-tab v) (xforms-tab v) v))

(defn do-all-xform [xforms-tab mp]
  (reduce (fn [hsh [k v]]
            (assoc hsh k (do-xform xforms k v)))
          {}
          mp))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn mk-vec [in] (THREE.Vector3. (:x in) (:y in) (:z in)))
(defn setpos! [cb v] (set! (.-position cb) (mk-vec v)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def renderer (THREE.WebGLRenderer.))
(def scene (THREE.Scene.) )
(def width (.-innerWidth js/window))
(def height (.-innerHeight js/window))
(def camera (THREE.PerspectiveCamera. 75 (/ width height) 0.1 1000 ))
(def geometry (THREE.CubeGeometry. 1 1 1))

(def material
  (THREE.MeshPhongMaterial. (clj->js {:color 0x00ff00 :shininess 100})))

(def r-material
  (THREE.MeshPhongMaterial. (clj->js {:shininess 100 :color 0xff0000})))

(def g-material
  (THREE.MeshPhongMaterial. (clj->js {:shininess 100 :color 0x00ff00})))

(def b-material
  (THREE.MeshPhongMaterial. (clj->js {:shininess 100 :color 0x0000ff})))

(def cube (THREE.Mesh. geometry shit-mat))

(defn mk-cube-mat [mat ^V/Vec3 v]
  (let [cb (THREE.Mesh. geometry mat)]
    (setpos! cb v)
    cb))

(defn mk-cube [v] (mk-cube-mat material v))
(defn add-geom [x] (.add scene x) x)

(def add-cube (comp add-geom mk-cube))

(def cam
  (atom (Cam/Cam. (math/vec3 0 0 10) (math/vec3 0 0 0) (math/vec3 0 0 0))))

(defn render[f]
  (js/requestAnimationFrame #(render f))
  (swap! cam f)
  (set! (.-position camera) (mk-vec (:pos @cam)))
  (set! (.-lookat camera) (mk-vec (:lookat @cam)))
  (set! (.-x (.-rotation cube))  (+ 0.01 (.-x (.-rotation cube))))
  (set! (.-y (.-rotation cube))  (+ 0.04 (.-y (.-rotation cube))))
  (set! (.-x (.-position cube))  (+ 0.001 (.-x (.-position cube))))
  (.render renderer scene camera))

(defn set-time [mat item v]
  (->  mat
    (aget "uniforms")
    (aget item)
    (aset "value" v)))

(defn mk-light []
  (let [light  (THREE.DirectionalLight. 0xffffff)
        dir    (.normalize (THREE.Vector3. 1 1 1))]
    (set! (.-position light) dir)
    light))

(defn test [mat]
  (let [pos (math/vec3 (- 5) 0 0)
        test-mesh (THREE.Mesh. (THREE.PlaneGeometry. 5 5 ) mat)]
    (jsu/log test-mesh)
    (.add scene test-mesh)))

(defn init [f ]
  (let []
    (.setSize renderer width height)
    (.appendChild js/document.body (.-domElement renderer) )
    (.add scene cube)
    (comment .add scene (mk-t-quad))
    (.add scene ( THREE.AmbientLight. 0x202020))
    (.add scene (mk-light))
    (render f)))




