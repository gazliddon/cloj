(ns gaz.macros)

(defmacro with-scene [scn & body]
  `(binding [~'gaz.three/*current-scene* ~scn]
    ~@body))

(defmacro with-rt [rt & body]
  `(binding [~'gaz.rendertarget/*current-rt*  ~rt]
     ~@body))
