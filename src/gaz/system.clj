(ns gaz.system
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require 
    [gaz.keys :as gkeys]
    [gaz.mouse :as mouse]
    [gaz.frame :as frame]
    [cljs.core.async :as ca]))

(def def-math ())
(def def-error ())
(defrecord IoChan [ in out])

(defn- mk-a-chan [f]
  (let [in-ch (ca/chan 10)] 
    (IoChan. in-ch (f in-ch))))

(defrecord Sys [key-chan
                mouse-chan
                frame-chan
                math
                error])

(def sys-atom (atom (Sys.
                      nil
                      nil
                      nil
                      def-math
                      def-error)))

(defn- get-chan [id] (id @sys-atom))
(defn get-key-chan [] (get-chan :keychan))
(defn get-mouse-chan [] (get-chan :mouschan ))

(defn init! []
  @sys-atom)

