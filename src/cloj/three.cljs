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
  (let [img (.loadTexture THREE.ImageUtils img-uri)
        mat (THREE.MeshLambertMaterial. (js-obj "map" img))]
    mat))

(defn mk-t-quad []
  (let 
    [plane    ( THREE.PlaneGeometry. 10 10)
     mesh     ( THREE.Mesh. plane (mk-img-mat "feedback.jpg")) ]
    mesh))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def renderer (THREE.WebGLRenderer.))
(def scene (THREE.Scene.) )
(def width (.-innerWidth js/window))
(def height (.-innerHeight js/window))
(def camera (THREE.PerspectiveCamera. 75 (/ width height) 0.1 1000 ))
(def geometry (THREE.CubeGeometry. 1 1 1))

(def material (THREE.MeshPhongMaterial. (clj->js {:color 0x00ff00 :shininess 100})))
(def material (THREE.MeshPhongMaterial. (clj->js {:color 0x00ff00 :shininess 100})))

(def r-material (THREE.MeshPhongMaterial. (clj->js {:shininess 100 :color 0xff0000})))
(def g-material (THREE.MeshPhongMaterial. (clj->js {:shininess 100 :color 0x00ff00})))
(def b-material (THREE.MeshPhongMaterial. (clj->js {:shininess 100 :color 0x0000ff})))

(defn mk-vec [in] (THREE.Vector3. (:x in) (:y in) (:z in)))

(def cube (THREE.Mesh. geometry material))
(defn setpos! [cb v] (set! (.-position cb) (mk-vec v)))


(defn mk-cube-mat [mat ^V/Vec3 v]
  (let [cb (THREE.Mesh. geometry mat)]
    (setpos! cb v)
    cb))

(defn mk-cube [v] (mk-cube-mat material v))
(defn add-geom [x] (.add scene x))

(def cam (atom (Cam/Cam. (math/vec3 0 0 10) (math/vec3 0 0 0) (math/vec3 0 0 0))))

(defn render[f]
  (js/requestAnimationFrame #(render f))
  (swap! cam f)
  (set! (.-position camera) (mk-vec (:pos @cam)))
  (set! (.-lookat camera) (mk-vec (:lookat @cam)))
  (set! (.-x (.-rotation cube))  (+ 0.01 (.-x (.-rotation cube))) )
  (set! (.-y (.-rotation cube))  (+ 0.04 (.-y (.-rotation cube))) )
  (set! (.-x (.-position cube))  (+ 0.001 (.-x (.-position cube))) )
  (.render renderer scene camera))

(defn set-time [mat item v]
  (->  mat
    ( aget "uniforms")
    ( aget item)
    ( aset "value" v)))

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

