(ns cloj.jsutil
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as dom]
            [goog.events :as events]
            [cljs.core.async :as ca]
            [gaz.system :as sys]
            [gaz.world :as world]
            [gaz.listen :as listen]
            [gaz.three :as three]
            [gaz.keys :as gkeys]
            [gaz.cam :as cam]
            [gaz.math :as math]
            [gaz.control :as control]))


(defn log [my-args]
     (.log js/console my-args))

