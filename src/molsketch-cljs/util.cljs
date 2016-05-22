(ns molsketch-cljs.util)

(declare distance distance-squared)

(defn clip-line [[x1 y1] [x2 y2] clip1 clip2]
  (let [l (distance [x1 y1] [x2 y2])
        dx1 (/ (* clip1 (- x2 x1)) l)
        dx2 (/ (* clip2 (- x1 x2)) l)
        dy1 (/ (* clip1 (- y2 y1)) l)
        dy2 (/ (* clip2 (- y1 y2)) l)]
      [[(+ x1 dx1) (+ y1 dy1)] [(+ x2 dx2) (+ y2 dy2)]]))

(defn distance-squared [[x1 y1] [x2 y2]]
  (+ (Math/pow (- x1 x2) 2)
    (Math/pow (- y1 y2) 2)))

(defn distance [p1 p2]
  (Math/sqrt (distance-squared p1 p2)))

(defn max-node [state]
  (apply max (keys (:nodes state))))

(defn max-bond [state]
  (apply max (keys (:bonds state))))

(defn max-molecule [state]
  (apply max (keys (:molecules state))))

(defn displacement [[x1 y1] [x2 y2]]
  [(- x2 x1) (- y2 y1)])

(defn normalize [[x y] len]
  (let [m (Math/sqrt (+ (Math/pow x 2) (Math/pow y 2)))]
    [(/ (* x len) m) (/ (* y len) m)]))



(defn rotate [[x y] degrees]
  (let [t (/ (* degrees (.-PI js/Math)) 180)]
      [(- (* x (Math/cos t)) (* y (Math/sin t)))
       (+ (* x (Math/sin t)) (* y (Math/cos t)))]))

; Matrix multiply with sqrt(1-a^2)     -a
;                           a       sqrt(1-a^2)
; representing a general rotation smaller than 90 degrees
(defn rotation-like [[x y] a]
  (let [A (Math/sqrt (- 1 (* a a)))]
    [(+ (* x A) (* y (- a)))
     (+ (* x a) (* y A))]))

(defn parse-mouse-event [ev]
  {:x (- (aget ev "pageX") 8)
   :y (- (aget ev "pageY") 8)
   :button (aget ev "button")})

(defn parse-keyboard-event [ev]
  {:key (char (aget ev "keyCode"))})

(defn distance-node [node point]
  (distance (:pos node) point))

(defn distance-line-section [[x1 y1] [x2 y2] point]
  (let [a (/ (- y2 y1) (- x2 x1))
        g (/ a (Math/sqrt (+ 1 (* a a))))
        [[x1 y1] [x2 y2] [x y]] (map #(rotation-like % (- g)) [[x1 y1] [x2 y2] point])]
    (cond
      (or (< x (min x1 x2)) (> x (max x1 x2)))
      (min (distance [x y] [x1 y1]) (distance [x y] [x2 y2]))
      :else (.abs js.Math (- y y2)))))

(defn distance-bond [state bond-id point]
  (let [node-ids (get-in state [:bonds bond-id :nodes])
        [p1 p2] (map #(get-in state [:nodes % :pos]) node-ids)]
    (distance-line-section p1 p2 point)))

(defn map-in [coll loc mapping]
  (update-in coll loc (fn [v] (into (empty v)
                                    (map mapping v)))))

(defn remap-nodes [fragment node-mapping]
  (-> fragment
      (map-in [:nodes] (fn [[n-id n]] [(node-mapping n-id) n]))
      (map-in [:bonds] (fn [[b-id b]] [b-id (map-in b [:nodes] node-mapping)]))))


(defn remap-bonds [fragment bond-mapping]
  (map-in fragment [:bonds] (fn [[b-id b]] [(bond-mapping b-id) b])))


(defn translate [fragment v]
  (map-in fragment [:nodes]
          (fn [[n-id n]] [n-id (update n :pos (partial mapv + v))])))

(defn invert [v]
  (mapv - v))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
(dissoc m k)))