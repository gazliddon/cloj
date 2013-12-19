;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Some util functions for cljs

(ns cloj.jsutil)

(defn log
  "TODO: need to make this multi args"
  [my-args]
  (.log js/console my-args))

(defn tojs
  "makes a javascript map from a clojure one"
  [cljmap]
  (let
    [out  ( js-obj)
     pout ( fn [k v]   ( aset out ( name k) v))
     mapf ( fn [[k v]]
            (if (map? v)
              (pout k (gaz-clj->js v))
              (pout k v)))]
    (doall (map mapf cljmap)) out))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;ends

