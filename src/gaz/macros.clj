(ns gaz.macros)

(defmacro with-scene [scn & body]
  `(binding [gaz.three/*current-scene* ~scn]
    ~@body))
