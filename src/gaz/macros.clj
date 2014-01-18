(ns gaz.macros)

(defmacro with-scene [scn & body]
  `(binding [gaz.three/*current-scene* ~scn]
    ~@body))

(ns gaz.rendertarget)

(defmacro with-rt [rt & body]
 `(binding [*current-rt* ~rt]
    ~@body))

