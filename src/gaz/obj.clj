(ns gaz.obj
  (:require
    [gaz.math :as math ]))

(defrecord Obj [pos vel])

(defn mk-obj [pos vel]
  (Obj. pos vel))

(defn is-dead? [obj]
  (:dead? obj))

(defn update [obj]
  (let [ufunc (:update obj)]
    (if ufunc 
      (ufunc obj)
      obj)))

(defn add-vels [obj]
  (assoc obj :pos (math/add  
                    (:pos obj) 
                    (:vel obj))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def objs (atom () ))

(defn add-obj! [obj]
  (swap! obj conj obj)
  obj)

(defn mk-add-obj! [pos vel]
  (add-obj! (mk-obj pos vel)))

(defn process-objs [objs]
  (->> objs
    (map (comp add-vels update))
    (filter is-dead?)))

(defn update-objs! []
  (swap! objs process-objs))

;; TODO add defaul init and and update funcs

(defn create-obj-from-typ [typ-record pos vel & rst]
  (let [ init-func (fn [obj] 
                     (do (apply (:init typ-record) obj rst) 
                       obj))]
    (-> (obj/mk-obj pos vel)
      (assoc :update (:update typ-record)
             :init   (:init typ-record))
      (init-func))))

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

 
