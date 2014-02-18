(ns objs.iterator
  (:require-macros
    [lt.macros                  :refer [behavior]])
  
  (:require
    [lt.object    :as object :refer [raise create object* merge!]]))

(defn- apply-index! [this fun]
  (let [{:keys [index]} @this ]
    (raise this :iterator.set-index! (fun index))))

(behavior ::get-index
          :triggers #{:iterator.get-index}
          :reaction (fn [this]
                      (let [{:keys [index col]} @this
                            amount    (raise col :iterable.get-count)
                            new-index (min (dec count) (max 0 index))]
                        new-index)))

(behavior ::set-index!
          :triggers #{:iterator.set-index!}
          :reaction (fn [this index]
                      (merge! this {:index (raise this :iterator.get-index)})))

(behavior ::next!
          :triggers #{:iterator.next!}
          :reaction  (fn [this] (apply-index! this inc)))

(behavior ::prev!
          :triggers #{:iterator.prev!}
          :reaction  (fn [this] (apply-index! this dec)))

(behavior ::set-wrap!
          :triggers #{:iterator.set-wrap!}
          :reaction  (fn [this wrap?] (merge! this {:wrap? wrap?})))

(behavior ::get-current
          :triggers #{:iterator.get-current}
          :reaction  (fn [this]
                       (let [index (raise this :iterator.get-index)]
                         (raise this :iterable.get-item index))))

(object* ::iterator
         :init (fn [this col index wrap?]
                 (merge! this {:col col})
                 (raise this :iterator.set-wrap! wrap? )
                 (raise this :iterator.set-index col index)))

(defn mk-iterator [col index]
  (create ::iterator col index))

