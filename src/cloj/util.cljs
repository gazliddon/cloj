(ns gaz.util)

(def pi (.-PI js/Math))

(def sin (.sin js/Math))
(def cos (.cos js/Math))

(def tau (* 2 pi))

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

