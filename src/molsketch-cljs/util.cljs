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
  (Math.sqrt (distance-squared p1 p2)))

(defn max-node [state]
  (apply max (keys (:nodes state))))

(defn max-bond [state]
  (apply max (keys (:bonds state))))

(defn max-molecule [state]
  (apply max (keys (:molecules state))))

(defn displacement [[x1 y1] [x2 y2]]
  [(- x2 x1) (- y2 y1)])

(defn add-vectors [vs])

(defn normalize [[x y] len]
  (let [m (Math/sqrt (+ (Math/pow x 2) (Math/pow y 2)))]
    [(/ (* x len) m) (/ (* y len) m)]))

(defn rotate [[x y] degrees]
  (let [t (/ (* degrees (.-PI js/Math)) 180)]
      [(- (* x (Math/cos t)) (* y (Math/sin t)))
       (+ (* x (Math/sin t)) (* y (Math/cos t)))]))

(defn parse-mouse-event [ev]
  {:x (- (aget ev "pageX") 8)
   :y (- (aget ev "pageY") 8)
   :button (aget ev "button")})

(defn parse-keyboard-event [ev]
  {:key (first (aget ev "key"))})
