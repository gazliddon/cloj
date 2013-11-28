;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Stuff for listenting to events from elements
(ns gaz.listen
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require 
            [goog.events :as events]
            [cljs.core.async :as ca]))

(defn listen
  "Create a channel to listen for an event
   on an element"
  [el type]
  (let [outchan (ca/chan)]
    (events/listen el type
      (fn [e] (ca/put! outchan e)))
    outchan))

(defn merge-listen [el events]
  (ca/merge (map #(listen el %) events)))

(defn listen-mouse [el]
  (merge-listen el ["mousemove" "mousedown" "mouseup"]))

(defn listen-keys
  "Create a merged channel of keydown and keyup events for an element"
  [el] (ca/merge [(listen el "keydown") (listen el "keyup")])) 

(defn on-keys
  "Call this function every time a keyup / keydown message is received for this element"
  [e f]
  (let [ kud (listen/listen-keys scr)]
    (go (while true
          (let [ k (ca/<! kud)]
            (f k))))))


