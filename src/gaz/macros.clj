(ns gaz.macros)
(defmacro aloop [[var arr] & body]
  `(let [arr# ~arr]
     (loop [~var 0]
       (when (< ~var (.-length arr#))
         ~@body
         (recur (+ ~var 1))))))
(defmacro with-scene [scn & body]
  `(binding [gaz.three/*current-scene* ~scn]
    ~@body))

(ns render.rendertarget)

(defmacro with-rt [rt & body]
 `(binding [*current-rt* ~rt]
    ~@body))

