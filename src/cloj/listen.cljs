;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Stuff for listenting to events from elements
(ns gaz.listen
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require 
    [cloj.jsutil :as jsu]
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

(defn merge-listen [el & events]
  (ca/merge (map #(listen el %) events)))

(defn listen-mouse [el]
  (merge-listen el "mousemove" "mousedown" "mouseup"))

(defn listen-keys
  "Create a merged channel of keydown and keyup events for an element"
  [el] (ca/merge [(listen el "keydown") (listen el "keyup")])) 

(defn on-keys
  "call func f for every key event on element e"
  [e f ]
  (let [ kud (listen-keys e)]
    (go (while true
          (if-let [ k (ca/<! kud)]
            (f k))))))



(defn get-all-key-events [dom-element]
  (merge-listen dom-element "keydown" "keyup"))


(defn topicfn [v]
  (let [typ (aget v "type")]
    (if (= typ "keyup")
      :yes
      :no)
    )
  )




(defn make-keys-pub [dom-element]
  (let [in-chan (get-all-key-events dom-element) ]
    (ca/pub in-chan topicfn)
    ))

; (defn test-it [dom-element]
;   (let [pub (make-keys-pub dom-element)
;         ch (ca/chan)
;         sub (ca/sub pub :yes ch)]

;     (go
;       (while true
;         (let [v (ca/<! ch)]
;           (jsu/log v)
;           )))))

; (defn test [out-c]
;   (go-loop [[x y] [0 0]]
;            (if-let [k (ca/<! key-chan) ]
;              (condp = k
;                :up      (recur [x (inc y)])
;                :down    (recur [x (dec y)])
;                :left    (recur [(dec x) y])
;                :right   (recur [(inc x) y])
;                :frame   (let [[x y] (clamp [x y])]
;                           (ca/put! out-c [x y])
;                           (recur [x y])) 
;                ))))
             
(defn test-it [])
