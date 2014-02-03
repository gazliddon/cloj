(ns math.vec3
  (:require
    [math.constants]
    ))

(def abs Math/abs)
(def sqrt Math/sqrt)

(defn mk-vec
  ([x y z] (array x y z))
  ([[x y z]](array x y z))
  ([] (array 0 0 0)))

(defn mk-vec-s [v] (mk-vec v v v ))

(defn dot [[x0 y0 z0] [x1 y1 z1]]
  (+ (* x0 x1) (* y0 y1) (* z0 z1) ))

(defn add-scalar [s v]
  (array 
    (+ s (aget v 0))
    (+ s (aget v 1))
    (+ s (aget v 2))))

(defn add [v0 v1]
  (array (+ (aget v0 0) (aget v1 0))
         (+ (aget v0 1) (aget v1 1)) 
         (+ (aget v0 2) (aget v1 2)) ))

(defn sub [v0 v1]
  (array (- (aget v0 0) (aget v1 0))
         (- (aget v0 1) (aget v1 1)) 
         (- (aget v0 2) (aget v1 2)) ))

(defn mul-scalar [s v0]
  (array (* (aget v0 0) s)
         (* (aget v0 1) s) 
         (* (aget v0 2) s) ))

(defn div-scalar [s v0]
  (array (/ s (aget v0 0) )
         (/ s (aget v0 1) ) 
         (/ s (aget v0 2) ) 
         )
  )

(defn add! [dest add]
  (aset dest 0 (+ (aget dest 0) (aget add 0)))
  (aset dest 1 (+ (aget dest 1) (aget add 1)))
  (aset dest 2 (+ (aget dest 2) (aget add 2)))
  dest)

(defn sub! [dest sub]
  (aset dest 0 (- (aget dest 0) (aget sub 0)))
  (aset dest 1 (- (aget dest 1) (aget sub 1)))
  (aset dest 2 (- (aget dest 2) (aget sub 2)))
  dest)

(defn mul-scalar! [s dest]
  (aset dest 0 (* (aget dest 0) s))
  (aset dest 1 (* (aget dest 1) s)) 
  (aset dest 2 (* (aget dest 2) s))
  dest)

(defn div-scalar! [s dest]
  (aset dest 0 (/ (aget dest 0) s))
  (aset dest 1 (/ (aget dest 1) s)) 
  (aset dest 2 (/ (aget dest 2) s))
  dest)

(defn copy! [dest src]
  (aset dest 0 (aget src 0))
  (aset dest 1 (aget src 1))
  (aset dest 2 (aget src 2))
  dest)

(defn mk-copy [src]
  (copy! (array ) src))

(def zero   (array 0 0 0))
(def x-axis (array 1 0 0))
(def y-axis (array 0 1 0))
(def z-axis (array 0 0 1))

(defn neg [v0] (sub zero v0))
(defn length-squared  [v] (dot v v))
(defn length  [v] (sqrt ( length-squared v)))
(defn unit-vector [v] (div-scalar (length v) v))

;; Clamping stuff
(defn- clamp-s
  "Return s-in clamped between s-in and s-max inclusive"
  [s-in s-min s-max]
  (min s-max (max s-in s-min)))

(defn clamp [[x y z] [min-x min-y min-z] [max-x max-y max-z]]
  (array (clamp-s x min-x max-x)
         (clamp-s y min-y max-y)
         (clamp-s z min-z max-z)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



