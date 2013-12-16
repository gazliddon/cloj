(ns gaz.keys
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [gaz.util :as util]
    [cljs.core.async :as ca]))

(def keytab (atom {}))

(defn- mk-key [on lst went-on went-off]
  {:state on :last lst :went-on went-on :went-off went-off})

(defn update-state [ {state :state} onoff]
  (let [went-on (and onoff (not state))
        went-off (and (not onoff) state)]
    (mk-key onoff state went-on went-off)))

(defn get-state [k]
  (if-let [k-state (find @keytab k)]
    (val k-state)
    (mk-key false false false false)))

(defn new-state! [k new-state]
    (swap! keytab #(assoc %1 k (update-state (get-state k) new-state))))

(defn pressed? [k] (:state (get-state k)))
(defn went-on? [k] (:went-on (get-state k)))
(defn went-off? [k] (:went-off (get-state k)))
(defn all-reset! [] (swap! keytab {}))

(defn filter-keys [f]
  (map first  (filter #(f (second %1)) @keytab) ))

(defn get-keys-on [] (filter-keys :state))
(defn get-keys-went-on [] (filter-keys :went-on))
(defn get-keys-went-off [] (filter-keys :went-off))
