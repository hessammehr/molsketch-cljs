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

(defn max-node [nodes]
  (apply max (keys nodes)))

(defn max-molecules [mols]
  (apply max (keys mols)))
