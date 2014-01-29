;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Some util functions for cljs

(ns cloj.jsutil)

(defn random [n]
  (* (.random js/Math) n))

(defn random-int [n]
  (bit-or (random n) 0))

(defn log [& rst]
  (doseq [t rst]
      (.log js/console t)))

(defn strkey
  "Helper fn that converts keywords into strings"
  [x]
  (if (keyword? x)
    (name x)
    x))

(extend-type object
  ILookup
  (-lookup
    ([o k]
       (aget o (strkey k)))
    ([o k not-found]
       (let [s (strkey k)]
         (if (goog.object.containsKey o s)
           (aget o s)
           not-found)))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;ends

