(ns ui.attrs
  (:require
    [gaz.math                 :as m]
    [cloj.jsutil              :as jsu]
    )
  )

(defrecord Attrs [data])

(defn get-attr
  ([o k] @(:value (o k)))
  ([o k nf] (if (o k)
              (get-attr o k)
              nf) )
  )

(def mkv3 m/mk-vec)

(defn fl
  ([v] {:type :float :value (atom v)})
  ([v min-v max-v ]{:type :float :value (atom v) :min min-v :max max-v} )
  ([] (fl 0)))

(defn v3
  ([v]
   {:type :v3 :value (atom (mkv3 v))})

  ([]
   {:type :v3 :value (atom (mkv3 0 0 0))})
 
  ([v min-v max-v]
   {:type :v3 :value (atom (mkv3 v)) :min (mkv3 min-v) :max (mkv3 max-v)}))


(defn mk-jso-from-attrs [mp]
  (let [ret (js-obj)]
    (jsu/log (str mp))
    (doseq [[k v] mp] 
      (aset ret (name k) @(:value v)))
    ret))

(defn float-to-gui [gui jso attrs k]
  (let [controller (.add gui jso (name k))]
    (doto controller
      (.onChange (fn [v] (reset! @( :value (attrs k)) v)))
      )))

(defn v3-to-gui [gui jso attrs k]
  (let [gui (.addFolder gui (name k))
        v3atom (:value  (attrs k)) ]

    (doseq [[nm i] [["x" 0] ["y" 1] ["z" 2]] ]
      (doto (.add gui (aget jso (name k)) i)
        (.name nm)
        (.onChange (fn[v] (reset! v3atom #(assoc %1 i v)) ))))))

(def type-to-gui-func
  {:float float-to-gui
   :v3 v3-to-gui})

(defn add-attrs-to-gui [gui attrs]
  (let [jso (mk-jso-from-attrs attrs)]
    (doseq [ [k v] attrs]
      ((type-to-gui-func (:type v)) gui jso attrs k)
      )))

