(ns ui.attrs
  (:require
    [math.vec3                  :as m]
    [cloj.jsutil                :as jsu]
    [gaz.util                   :as util]
    [clojure.string             :as string]
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
  ([v] {:type :v3 :value (atom (mkv3 v))})

  ([] {:type :v3 :value (atom (mkv3 0 0 0))})
 
  ([v min-v max-v]
   {:type :v3 :value (atom (mkv3 v)) :min (mkv3 min-v) :max (mkv3 max-v)}))

(defn tex2d [v] {:type :tex2d :value (atom v)})

(defn mk-jso-from-attrs [mp]
  (let [ret (js-obj)]
    (jsu/log (str mp))
    (doseq [[k v] mp] 
      (aset ret (name k) @(:value v)))
    ret))

(defn float-to-gui [gui jso attrs k]
  (let [controller (.add gui jso (name k))]
    (doto controller
      (.onChange
        (fn [v] (reset! ( :value (attrs k)) v))))))

(defn v3-to-gui [gui jso attrs k]
  (let [gui    (.addFolder gui (name k))
        v3atom (:value  (attrs k)) ]

    (doseq [[nm i] [["x" 0] ["y" 1] ["z" 2]] ]
      (doto (.add gui (aget jso (name k)) i)
        (.name nm)
        (.onChange (fn[v] (aset @v3atom i v)))))))

(def type-to-gui-func
  {:float float-to-gui
   :v3 v3-to-gui})

(defn add-attrs-to-gui [gui attrs]
  (let [jso (mk-jso-from-attrs attrs)]
    (doseq [ [k v] attrs]
      ((type-to-gui-func (:type v)) gui jso attrs k))))



(def type-to-uni-type {:float "f"
                       :vec3  "v"
                       :tex2d "t"
                       })

(defn uni-xform [v]
  (if-let [typ (type-to-uni-type (:type v)) ]
    (js-obj "type" typ "value" @(:value v))
    nil))

(defn mk-material-init-from-uniforms [attrs]
  (let [ret (js-obj)]
    (doseq [[k v] attrs]
      (when-let [uni (uni-xform v)]
        (aset ret (name k) uni)) )
    ret
    ))

(def get-type
  {:float "float"
   :vec3 "vec3"
   :tex2d "sampler2D"})

(defn attr-to-uniform-str [k v]
  (if-let [str-type ((:type v) get-type)]
    (str "uniform " str-type " " (name k) ";")
    nil))

(defn attrs-to-uniform-str [unis]
  (let [ mp (map (fn [[k v]] (attr-to-uniform-str k v)) unis) ]
    (string/join "\n" mp)))

