(ns gaz.three

  (:require-macros [gaz.macros :refer [with-scene]])
  (:require
    [gaz.gamescreen :as gs :refer [mk-game-screen-in-window] ]
    [cloj.jsutil :as jsu ]
    [gaz.math :as math :refer [mk-vec]]
    [gaz.feedback :as fb ]
    [gaz.cam :as Cam]))

;; Current scene stuff
(def ^:dynamic *current-scene* nil)

(defn add [o]
  (.add *current-scene* o))

(defn render [rt cam]
  (.render rt *current-scene* cam))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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

(def three-materials {
                      :phong (fn [v] THREE.MeshPhongMaterial. v)})


(def materials {:red-flat   {:three-material :fong
                             :color 0xff0000
                             :shininess 100 }

                :green-flat {:three-material :fong
                             :color 0x00ff00
                             :shininess 100 }

                :blue-flat  {:three-material :fong
                             :color 0x0000ff
                             :shininess 100 }

                :feedback-img {:three-material :fong
                               :color 0x00ff00
                               :shininess 100
                               :map "feedback.jpg" }})

(def xforms
  {:map #(.loadTexture THREE.ImageUtils %1) })

(defn do-xform [xforms-tab k v]
  (if (xforms-tab v) (xforms-tab v) v))

(defn do-all-xform [xforms-tab mp]
  (reduce (fn [hsh [k v]]
            (assoc hsh k (do-xform xforms k v)))
          {}
          mp))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn mk-three-vec [in] (THREE.Vector3. (:x in) (:y in) (:z in)))
(defn set-pos! [cb v] (set! (.-position cb) (mk-three-vec v)))
(defn set-rot! [cb v] (set! (.-rotation cb) (mk-three-vec v)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def renderer (THREE.WebGLRenderer.))
(def scene (THREE.Scene.) )

(def width (.-innerWidth js/window))
(def height (.-innerHeight js/window))
(def wd2 (/ width 2))
(def hd2 (/ height 2))
(def aspect-ratio (/ width height))

(def sscale 5.0)
(def camera (THREE.OrthographicCamera.
              (- (* aspect-ratio sscale ))
              (* aspect-ratio sscale )
              sscale
              (- sscale)
              0.1 1000 ))
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
    (set-pos! cb v)
    cb))

(defn mk-cube [v] (mk-cube-mat material v))
(defn add-geom [x] (.add scene x) x)

(def add-cube (comp add-geom mk-cube))

(def cam
  (atom (Cam/Cam. (math/vec3 0 0 10) (math/vec3 0 0 0) (math/vec3 0 0 0))))

(defn render[f]
  (js/requestAnimationFrame #(render f))
  (swap! cam f)
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

(defn mk-scene [] (THREE.Scene.))

(def game-screen (atom nil))

(defn init [f ]
  (let []
    (.setSize renderer width height)
    (.appendChild js/document.body (.-domElement renderer) )
    
    (with-scene scene
                (add ( THREE.AmbientLight. 0x202020))
                (add (mk-light)))
    (render f)))


