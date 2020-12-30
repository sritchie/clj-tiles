(ns cljtiles.xmlparse-2)

(defmulti tag-m :tag)

(defmulti type-m (fn [attributes _content]
                   (subs (str (:type attributes) "____") 0 4)))

(defmethod tag-m :default [x] x)

(defmethod tag-m :xml [{:keys [content]}]
  (mapv tag-m content))

(defmethod tag-m :block [{:keys [attributes content]}]
  (type-m attributes content))

(defmethod tag-m :value [{:keys [content]}]
  (tag-m (first content)))

(defmethod tag-m :field [{:keys [content]}]
  (first content))

(defmethod type-m :default [_ content] content)

(defmethod type-m "text" [_ content]
  (or (first (:content (first content))) " "))

(defmethod type-m "funs" [_ content]
  (let [erg
        (apply list (symbol (first (:content (first content))))
               (map tag-m (rest content)))
        augment-arg (fn [e] (if (vector? e) e [e]))]
    (if (= 'defn (first erg))
      (apply list 'defn (nth erg 1) (augment-arg (nth erg 2))
             (drop 3 erg))
      erg)))

(defmethod type-m "num_" [_ content]
  (symbol (first (:content (first content)))))

(defmethod type-m "infi" [_ content]
  (type-m {:type "funs"} content))

(defmethod type-m "args" [_ content]
  (mapv tag-m content))

(defmethod type-m "list" [_ content]
  (map tag-m content))

(defn to-hashmap [v]
  (let [c (/ (count v) 2)]
    (into {} (map (fn [k v] [(symbol k) v])
                  (take c v)
                  (take-last c v)))))

(defmethod type-m "map-" [_ content]
  (to-hashmap (map tag-m content)))

(defn parse [edn inspect-id]
  (tag-m edn))
