(ns content.effect)

(defprotocol Effect
  (update [_ tm])
  (get-output [_])
  )
