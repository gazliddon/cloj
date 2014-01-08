(ns gaz.math2)

(defn init! [v])
(defn mk-vec
  ([x y z] (array x y z)))

(defn dot [[x0 y0 z0] [x1 y1 z1]]
  (+ (* x0 x1) (* y0 y1) (* z0 z1) ))

(defn add [v0 v1]
  (array (+ (aget v0 0) (aget v1 0))
         (+ (aget v0 1) (aget v1 1)) 
         (+ (aget v0 2) (aget v1 2)) 
         )
  )

(defn sub [v0 v1]
  (array (- (aget v0 0) (aget v1 0))
         (- (aget v0 1) (aget v1 1)) 
         (- (aget v0 2) (aget v1 2)) 
         )
  )

(defn mul-scalar [s v0]
  (array (* (aget v0 0) s)
         (* (aget v0 1) s) 
         (* (aget v0 2) s) 
         )
  )

(defn div-scalar [s v0]
  (array (/ s (aget v0 0) )
         (/ s (aget v0 1) ) 
         (/ s (aget v0 2) ) 
         )
  )

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
(def epsilon 2.220460492503130808472633361816E-16)
(defn is-equal [a b] (<= (abs (- a b) epsilon)))
(defn is-zero [v] (is-equal 0 v))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment 

  ;; This was faster than maps but still created
  ;; a persistent vector for each vec3

  (ns gaz.math2)

  (defn init! [v])
  (defn mk-vec
    ([x y z] [x y z]))

  (defn dot [v0 v1]
    (let [[x0 y0 z0] v0
          [x1 y1 z1] v1]
      (+ (* x0 x1) (* y0 y1) (* z0 z1) )))

  (defn add [v0 v1]
    (let [[x0 y0 z0] v0
          [x1 y1 z1] v1]
      [(+ x0 x1) (+ y0 y1) (+ z0 z1)] ))

  (defn sub [v0 v1]
    (let [[x0 y0 z0] v0
          [x1 y1 z1] v1]
      [ (- x0 x1) (- y0 y1) (- z0 z1)]))

  (defn mul-scalar [s v0]
    (let [[x0 y0 z0] v0 ]
      [ (* s x0 ) (* s y0 ) (* s z0 )]))

  (defn div-scalar [s v0]
    (let [[x0 y0 z0] v0 ]
      [ (/ x0 s) (/ y0 s) (/ z0 s)]))
  
  )


