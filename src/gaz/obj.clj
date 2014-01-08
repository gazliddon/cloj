(ns gaz.obj
  (:require
    [cloj.jsutil     :as jsu]
    [gaz.math2 :as math ]))

(defprotocol UpdateObject
  (update [_ tm])
  (vel [_])
  (pos [_])
  (add-vel! [_ tm])
  (is-dead? [_]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def objs (atom [] ))

(defn add-obj! [obj]
  (swap! objs conj obj)
  obj)

(defn update-objs! [tm]
  (let [out (transient [])]
    (doseq [o @objs]
      (conj! out (update o tm)))
    (reset! objs (persistent! out))) )

(defn update-objs! [tm]
  (dorun 
    (reset! objs (map #(update %1 tm) @objs))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn- get-oscillate-vel [pos vel origin scale]
  (->> pos
    (math/sub origin)
    (math/mul-scalar scale)
    (math/add vel)))

(defn home! [homer home-pos scale]
  (assoc homer :vel (get-oscillate-vel
                      (:pos homer)
                      (:vel homer)
                      home-pos
                      scale)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

