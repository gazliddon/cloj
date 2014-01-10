(ns gaz.system
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require 
    [cljs.core.async :as ca]))

(def sys-atom (atom {}))
(defn init! []
  @sys-atom)

