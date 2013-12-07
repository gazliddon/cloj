(ns gaz.mouse
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require 
    [cljs.core.async :as ca]))

(defn- ev-to-message [buttonstate event]
  (if (and buttonstate (:ev :mousemove))
    (assoc event :event :mousedrag)
    event))

(defn- get-new-button-state [buttonstate event]
  (condp (:event buttonstate)
    :mousedown true
    :mouseup false
    :focuslost false
    buttonstate))

(defn mk-chan [in-channel]
  (let [out-channel (ca/chan 2)]
    (go-loop [heldown false]
             (let [ev < ca/<! in-channel
                   cooked-ev (ev-to-message heldown ev)]
               (ca/>! out-channel cooked-ev)
               (recur (get-new-button-state heldown cooked-ev))))))


