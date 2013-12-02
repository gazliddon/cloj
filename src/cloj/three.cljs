(ns gaz.three
  (:require 
    [gaz.math :as math ]
    [gaz.cam :as Cam]))

(def THREE js/THREE)

(def renderer (THREE.WebGLRenderer.))
(def scene (THREE.Scene.) )
(def width (.-innerWidth js/window))
(def height (.-innerHeight js/window))
(def camera (THREE.PerspectiveCamera. 75 (/ width height) 0.1 1000 ))
(def ^:const geometry (THREE.CubeGeometry. 1 1 1))

(def material (THREE.MeshBasicMaterial. (clj->js {:color 0x00ff00})))

(def r-material (THREE.MeshBasicMaterial. (clj->js {:color 0xff0000})))
(def g-material (THREE.MeshBasicMaterial. (clj->js {:color 0x00ff00})))
(def b-material (THREE.MeshBasicMaterial. (clj->js {:color 0x0000ff})))

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

(defn- logit [x](. js/console (log x)))

(defn render[f]
            (js/requestAnimationFrame #(render f))
            (swap! cam f)
            (set! (.-position camera) (mk-vec (:pos @cam)))
            (set! (.-lookat camera) (mk-vec (:lookat @cam)))
            (set! (.-x (.-rotation cube))  (+ 0.01 (.-x (.-rotation cube))) )
            (set! (.-y (.-rotation cube))  (+ 0.04 (.-y (.-rotation cube))) )
            (set! (.-x (.-position cube))  (+ 0.001 (.-x (.-position cube))) )
            (.render renderer scene camera))

(defn init [f]
  (let []
    (logit  @cam )
    (.setSize renderer width height)
    (.appendChild js/document.body (.-domElement renderer) )
    (.add scene cube)
    (render f)))

