(ns gaz.math
  (:require [gaz.util :refer [update-map]]))

(defn- default-abs [v]
  (if (< v 0)
    (- 0 v)
    v))

(defn- default-sqrt [v]
   0)

(def math-atom (atom { :abs  default-abs
                       :sqrt default-sqrt }))

(defn init!
  "Initialise math module with defs for routines needed
   Works with cljs and clj where abs / sqrt and a few
   other things aren't the same"
  
  [mth] (do 
          (swap! math-atom update-map mth)
          @math-atom))


(defrecord Vec3 [^double x ^double y ^double z])

(defn mk-vec
  ([[x y z]] (Vec3. x y z))
  ([x y z]   (Vec3. x y z)))

(defn abs [v] ( (:abs @math-atom) v))
(defn sqrt [v] ( (:sqrt @math-atom) v))


(defn vec3 [^double x ^double y ^double z]
  (Vec3. x y z))

(defn dot ^double [^Vec3 v0 ^Vec3 v1]
  (+ (*(:x v0)(:x v1)) (*(:y v0)(:y v1)) (*(:z v0) (:z v1))))

(defn applyf ^Vec3 [^Vec3 v0 f]
  (Vec3. (f (:x v0)) (f (:y v0)) (f (:z v0))))

(defn applyvf ^Vec3 [^Vec3 v0 ^Vec3 v1 f]
  (Vec3. (f (:x v0) (:x v1)) (f (:y v0)(:y v1)) (f (:z v0)(:z v1))))

(defn mul ^Vec3 [^Vec3 v0 ^Vec3 v1] (applyvf v0 v1 * ))
(defn sub ^Vec3 [^Vec3 v0 ^Vec3 v1] (applyvf v0 v1 - ))
(defn add ^Vec3 [^Vec3 v0 ^Vec3 v1] (applyvf v0 v1 + ))

(defn mul-scalar ^Vec3 [s0 ^Vec3 v0] (applyf v0 (partial * s0) ))
(defn div-scalar ^Vec3 [s0 ^Vec3 v0] (applyf v0 (partial * (/ 1.0 s0)) ))

(def zero   ^Vec3 (Vec3. 0 0 0))
(def x-axis ^Vec3 (Vec3. 1 0 0))
(def y-axis ^Vec3 (Vec3. 0 1 0))
(def z-axis ^Vec3 (Vec3. 0 0 1))

(defn neg ^Vec3 [^Vec3 v0] (sub zero v0))

(defn length-squared ^double [v] (dot v v))
(defn length ^double [v] (sqrt ( length-squared v)))
(defn unit-vector ^Vec3 [^Vec3 v] (div-scalar (length v) v))

;; Clamping stuff
(defn- clamp-s
  "Return s-in clamped between s-in and s-max inclusive"
  [s-in s-min s-max]
  (min s-max (max s-in s-min)))

(defn- clamp-id 
  "Clamp the id f in these records"
  [r-in r-min r-max f]  (clamp-s (f r-in) (f r-min) (f r-max)))

(defn ^Vec3 clamp [^Vec3 v-in ^Vec3 v-min ^Vec3 v-max]
  (let [clamper (partial clamp-id v-in v-min v-max)]
    (Vec3. (clamper :x) (clamper :y) (clamper :z))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def epsilon 2.220460492503130808472633361816E-16)
(defn is-equal [a b] (<= (abs (- a b) epsilon)))
(defn is-zero [v] (is-equal 0 v))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Planes and interesections

;; Rays
(defrecord Ray [pos dir])
(defn ray ^Ray [pos dir]
  (Ray. pos (unit-vector dir)))

;; Planes
(defrecord Plane [^Vec3 normal ^double d ^double negd])
(defn plane 
  ^Plane [^Vec3 pos ^Vec3 n]
  (Plane. n (dot pos n) (- (dot pos  n))))

;; Intersections
(comment defn ray-intersects-with-plane [ray plane]
  (let [ {:keys [normal negd]} plane
         {:keys [pos dir]} ray
         numer(- negd (0 - (dot pos normal)))
         denom(dot dir normal)]
    (not  (and (is-zero numer) (is-zero denom) ))))

(defn ray-distance-to-plane [^Ray ray ^Plane plane]
  (let [ {:keys [normal negd]} plane
         {:keys [pos dir]} ray ]
    (/ (- negd (- (dot pos normal)))
       (dot dir normal))))

(defn ray-intersection-with-plane [ray plane]
  (let [{:keys [pos dir]} ray]
    (sub pos (mul-scalar (ray-distance-to-plane ray plane) dir ))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ends

