(ns gaz.obj
  (:require
    [cloj.jsutil     :as jsu]
    [gaz.math :as math ]))

(defrecord Obj [pos vel])

(defn mk-obj [pos vel]
  (Obj. pos vel))

(defn is-dead? [obj]
  (if (nil? (:dead obj))
    false
    (:dead obj)))

(defn update [obj]
  (let [ufunc (:update obj)]
    (if ufunc (ufunc obj))
    obj))

(defn add-vels [obj]
  (assoc obj :pos (math/add
                    (:pos obj)
                    (:vel obj))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def objs (atom () ))

(defn add-obj! [obj]
  (swap! objs conj obj)
  obj)

(defn mk-add-obj! [pos vel]
  (add-obj! (mk-obj pos vel)))

(defn lo-update-objs! []
  (swap! objs (fn [obj-list]
                (->> obj-list 
                  (map (comp add-vels update))))))

(defn update-objs! []
  (doall 
    (lo-update-objs!)) )

;; TODO add defaul init and and update funcs

(defn create-obj-from-typ [typ-record pos vel & rst]
  (let [init (:init typ-record)
        obj (assoc
              (mk-obj pos vel)
              :update (:update typ-record)
              :init init)]
    (init obj pos vel rst)
    obj))

(defn add-obj-from-typ! [typ-record pos vel & rst]
  (add-obj! (create-obj-from-typ typ-record pos vel rst)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- get-oscillate-vel [pos vel origin scale]
  (->> pos
    (math/sub origin)
    (math/mul-scalar scale)
    (math/add vel)))

(defn home [home-to homer scale]
  (assoc homer :vel (get-oscillate-vel
                      (:pos homer)
                      (:vel homer)
                      (:pos home-to)
                      scale)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

