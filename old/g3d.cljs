(ns cloj.g3d
  (:require-macros [cljs.core.async.macros :refer [go]])

  (:require [cljs.core.async :as ca]
            [cloj.jsutil     :as jsu]
            [gaz.math         :as math]))


(defn- vec-to-tvec [in] (THREE.Vector3. (:x in) (:y in) (:z in)))

(defn set-atttr-to-vec! [attr obj v]
  (aset attr obj v 
    obj))

(defn- set-position! [obj v]
  (set-atttr-to-vec! "position" obj v)  )

(defn mk-mesh [func pos mat]
  (let [geo   (func)
        mesh (THREE.Mesh. geo mat)]
    (set-atttr-to-vec! "position" mesh pos)))

(defn mk-cube [pos mat]
  (mk-mesh #(THREE.CubeGeometry. 1 1 1 1 1 1) pos mat))


