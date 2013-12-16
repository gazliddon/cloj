(ns gaz.three
  (:require 
    [cloj.jsutil :as jsu ]
    [gaz.math :as math ]
    [gaz.cam :as Cam]))

(def THREE js/THREE)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn gaz-clj->js
  "makes a javascript map from a clojure one"
  [cljmap]
  (let [out (js-obj)
        pout (fn [k v] (aset out (name k) v))
        mapf (fn [[k v]]
               (if (map? v)
                   (pout k (gaz-clj->js v))
                   (pout k v)))]
    (doall (map mapf cljmap)) out))

(defn mk-three-shader [shad]
  (THREE.ShaderMaterial. (gaz-clj->js shad)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Feedback Stuff

(defrecord Feedback [cam scene-0 scene-1])
(defrecord OffScreen [scene rt])

(defn mk-render-target [width height]
  (THREE.WebGLRenderTarget.
    width height
    (gaz-clj->js {:minFilter  THREE.LinearFilter
                  :magFilter  THREE.LinearFilter
                  :format     THREE.RGBFormat })))

(defn mk-quad-mesh [width height material]
  (THREE.Mesh (THREEE.PlaneGeometry. width height) material))

(defn mk-offscr-material [mat previous-rt ]
  (mk-three-shader (assoc mat :prevScreen {:type "t" :value previous-rt })))

(defn mk-scene [mat width height my-rt previous-rt]
  (let [msh (mk-quad-mesh width height (mk-offscr-material mat previous-rt))
        scn (THREE.Scene.) ]
    (.add scn msh)
    OffScreen. scn my-rt))

(defn mk-feedback [material width height]

  (let [[wd2 hd2] [(/ width 2.0) (/ height 2.0)]
        mk-scn    (partial mk-scene material width height)
        plane     (THREE.PlaneGeometry. width height)
        rt0       (mk-render-target width height)
        rt1       (mk-render-target width height) ]

    (Feedback.
      (THREE.OrthographicCamera. (- wd2) wd2 hd2 (- hd2) 0 1000)
      (mk-scn rt0 rt1)
      (mk-scn rt1 rt0))))

(defn render-feedback [r t [cam scn-0 scn-1]]
  (.clear r)
  (.render
    (:sceen scn-0) cam (:rt scn-0, true))
  (Feedback.  cam scn-1 scn-1))

(def tojs gaz-clj->js)

(defn mk-t-quad []
  (let 
    [plane    ( THREE.PlaneGeometry. 10 10)
     img      ( .loadTexture THREE.ImageUtils "feedback.jpg")
     jsmap    ( js-obj "map" img)
     material ( THREE.MeshLambertMaterial. jsmap )
     mesh     ( THREE.Mesh. plane material) ]
    mesh))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def renderer (THREE.WebGLRenderer.))
(def scene (THREE.Scene.) )
(def width (.-innerWidth js/window))
(def height (.-innerHeight js/window))
(def camera (THREE.PerspectiveCamera. 75 (/ width height) 0.1 1000 ))
(def geometry (THREE.CubeGeometry. 1 1 1))

(def material (THREE.MeshPhongMaterial. (clj->js {:color 0x00ff00 :shininess 100})))

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
  (let [light (THREE.DirectionalLight. 0xffffff)
        dir (.normalize (THREE.Vector3. 1 1 1))]
    (set! (.-position light) dir)
    light))

(defn init [f]
  (let []
    (.setSize renderer width height)
    (.appendChild js/document.body (.-domElement renderer) )
    (.add scene cube)
    (.add scene (mk-t-quad))
    (.add scene ( THREE.AmbientLight. 0x202020))
    (.add scene (mk-light))
    (render f)))

