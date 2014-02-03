(ns gaz.util)

(def not-nil? (comp not nil?))

(defn filter-out-nil
  "Remove all elements in this sequence that are nil"
  [xs] (filter not-nil? xs))

(defn combine
  "Combines two sequences of sequences of the same size with a function
   (combine + [1 2 3] [3 4 6]) = [4 6 9]"
  [f a b] (map #(f %1 %2) a b))

(defn add-seqs
  "add a sequence of same sized sequences together"
  [xs](reduce #(combine + %1 %2) xs))

(defn map-kv 
  "map f over each key val in map"
  [f hm] (map #(f % (hm %)) (keys hm)))

(defn update-map 
  "Update map m1 with new key/vals from map m2"
  [m1 m2]
  (let [new-kv (map-kv #(list %1 %2) m2)]
    (reduce #(assoc %1 (first %2) (second %2)) m1 new-kv)))

