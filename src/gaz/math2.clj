(ns gaz.math2)

(defn init! [v])
(defn mk-vec
  ([x y z] (array x y z)))

(defn dot [v0 v1]
  (let [[x0 y0 z0] v0
        [x1 y1 z1] v1]
  (+ (* x0 x1) (* y0 y1) (* z0 z1) )))

(defn add [v0 v1]
  (let [[x0 y0 z0] v0
        [x1 y1 z1] v1]
    (array (+ x0 x1) (+ y0 y1) (+ z0 z1)) ))

(defn sub [v0 v1]
  (let [[x0 y0 z0] v0
        [x1 y1 z1] v1]
    (array (- x0 x1) (- y0 y1) (- z0 z1))))

(defn mul-scalar [s v0]
  (let [[x0 y0 z0] v0 ]
    (array (* s x0 ) (* s y0 ) (* s z0 ))))

(defn div-scalar [s v0]
  (let [[x0 y0 z0] v0 ]
    (array (/ x0 s) (/ y0 s) (/ z0 s))))

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


