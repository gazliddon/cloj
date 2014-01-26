(ns gaz.col)

(defn get-col [val bit-pos]
  (let [shifted (bit-shift-right val bit-pos)
        anded (bit-and shifted 0xff)]
    (/ anded 255.0)))

(defn hex-col-to-vec [hx]
  (let [r (get-col hx 0)
        g (get-col hx 8)
        b (get-col hx 16)
        ]
    [r g b] ) )
