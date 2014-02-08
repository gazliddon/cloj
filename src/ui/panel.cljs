(ns ui.panel
  (:require
    [gaz.three  :as three]))

(def width-segments 10)
(def height-segments 10)
(def width 10)
(def height 10)
(def x-step (/ width width-segments))
(def y-step (/ height height-segments))

(defrecord Panel [pos objs])

(defn- mk-phong [color shininess]
 (js/THREE.MeshPhongMaterial.
    (js-obj
      "color" color
      "shininess" shininess)))

(def white-mat (mk-phong 0xaaaaaa 200))
(def grey-mat  (mk-phong 0x222222 200))

(defn- white? [x y] (and (even? x) (odd? y)))

(defn- mat [x y]
  (if (white? x y)
    white-mat
    grey-mat))

(def panel-geo 
  (js/THREE.PlaneGeometry. x-step y-step 1 1))

(defn- mk-panel-mesh [mat x y]
  (let [msh (js/THREE.Mesh. panel-geo mat)]
    (three/set-pos! msh (array x y 0))
    msh))

(defn- mk-panel-meshes []
  (for [x (range width-segments)
        y (range height-segments)]
    (mk-panel-mesh (* x-step x) (* y-step y) (mat x y))))

(defn mk-panel-grp [] (comp three/mk-geo-group mk-panel-meshes))

(defn mk-panel-obj [pos]
  (three/set-pos! (mk-panel-grp) pos))


