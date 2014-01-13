(ns kioo.core)


(defn flatten-nodes [nodes]
  (reduce #(if (seq? %2)
             (concat %2 %1)
             (conj %1 %2))
          '()
          (reverse nodes)))

(defn make-react-dom [node & body]
  (let [rnode (if (map? node)
                (apply (:sym node)
                 (clj->js (:attrs node))
                 (flatten-nodes (:content node)))
                node)]
    (if (empty? body)
      rnode
      (cons rnode (make-react-dom body)))))

(defn content [& body]
  (fn [node]
    (assoc node :content body)))

(defn content [& body]
  (fn [node]
    (assoc node :content body)))

(defn append [& body]
  (fn [node]
    (assoc node :content (concat (:content node) body))))

(defn prepend [& body]
  (fn [node]
    (assoc node :content (concat body (:content node)))))

(defn after [& body]
  (fn [node]
    (cons node body)))

(defn before [& body]
  (fn [node]
    (concat body [(make-react-dom node)])))


(defn set-attr [& body]
  (let [els (partition 2 body)]
    (fn [node]
      (assoc node :attrs (reduce (fn [n [k v]]
                                  (assoc n k v))
                                (:attrs node) els)))))

(defn remove-attr [& body]
  (fn [node]
    (assoc node :attrs (reduce (fn [n k]
                                (dissoc n k))
                              (:attrs node) body))))

(defn do-> [& body]
  (fn [node]
    (reduce #(%2 %1) node body)))


(defn set-style [& body]
  (let [els (partition body 2)
        mp (reduce (fn [m [k v]] (assoc m k v)) {} els)]
    (fn [node]
      (update-in node [:attrs :style] #(merge %1 mp)))))


(defn remove-style [& body]
  (apply set-style  (interleave body (repeat nil))))


