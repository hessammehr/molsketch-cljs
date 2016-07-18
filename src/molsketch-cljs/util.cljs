(ns molsketch-cljs.util)

(declare distance distance-squared)

(defn clip-line [[x1 y1] [x2 y2] clip1 clip2]
  (let [l (distance [x1 y1] [x2 y2])
        dx1 (/ (* clip1 (- x2 x1)) l)
        dx2 (/ (* clip2 (- x1 x2)) l)
        dy1 (/ (* clip1 (- y2 y1)) l)
        dy2 (/ (* clip2 (- y1 y2)) l)]
      [[(+ x1 dx1) (+ y1 dy1)] [(+ x2 dx2) (+ y2 dy2)]]))

(defn len [x y]
  (+ (* x x)
     (* y y)))

(defn displacement [[x1 y1] [x2 y2]]
  [(- x2 x1) (- y2 y1)])

(defn distance-squared [p1 p2]
  (len (displacement p1 p2)))

(defn distance [p1 p2]
  (Math/sqrt (distance-squared p1 p2)))

(defn normalize [[x y] len]
  (let [m (len [x y])]
    [(/ (* x len) m) (/ (* y len) m)]))

(defn invert [v]
  (mapv - v))

(defn distance-node [node point]
  (distance (:pos node) point))

(defn distance-line-section [[x1 y1] [x2 y2] point]
  (let [a (/ (- y2 y1) (- x2 x1))
        g (/ a (Math/sqrt (+ 1 (* a a))))
        [[x1 y1] [x2 y2] [x y]]
        (map #(rotation-like % (- g)) [[x1 y1] [x2 y2] point])]
    (cond
      (or (< x (min x1 x2)) (> x (max x1 x2)))
      (min (distance [x y] [x1 y1]) (distance [x y] [x2 y2]))
      :else (.abs js/Math (- y y2)))))

(defn distance-bond [state bond-id point]
  (let [node-ids (get-in state [:bonds bond-id :nodes])
        [p1 p2] (map #(get-in state [:nodes % :pos]) node-ids)]
    (distance-line-section p1 p2 point)))

(defn max-node [state]
  (apply max (keys (:nodes state))))

(defn max-bond [state]
  (apply max (keys (:bonds state))))

(defn max-molecule [state]
  (apply max (keys (:molecules state))))

(defn angle [[x y]]
  (let [y (- y)
        a (Math/atan (/ y x))
        a (if (pos? x) a (+ a (.-PI js/Math)))]
    (/ (* a 180) (.-PI js/Math))))

(defn translate [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn rotate-degrees
  ([[x y] degrees & {[ox oy] :origin :or {[ox oy] [0 0]}}]
   (let [x (- x ox)
         y (- y oy)
         t (/ (* degrees (.-PI js/Math)) 180)]
     [(+ ox (- (* x (Math/cos t)) (* y (Math/sin t))))
      (+ oy (+ (* x (Math/sin t)) (* y (Math/cos t))))])))

; Matrix multiply with      sr     s*sqrt(1-r^2)
;                     -s*sqrt(1-a^2)     sa
; representing a general rotation and scaling operation
(defn matrix-transform
  [[x y] r s & {[ox oy] :origin :or {[ox oy] 0 0}}
   (let [x (- x ox)
         y (- y oy)
         a (* r s)
         A (Math/sqrt (- (* s s) (* a a)))]
     [(+ ox (+ (* x a) (* y A)))
      (+ oy (- (* y a) (* x A)))])])

; Returns a unitary transformation that orients
; [x1 y1] along [x2 y2] by rotation and scaling
(defn xform-from-to [[x1 y1] [x2 y2]]
  (let [l1 (+ (* x1 x1) (* y1 y1))
        l2 (+ (* x2 x2) (* y2 y2))
        [x2 y2] (map #(* % (Math/sqrt (/ l1 l2)))
                 [x2 y2]) ; rescale [x2 y2] to be the same length as [x1 y1]
        a (/ (+ (* x1 x2) (* y1 y2)) l1)
        b (/ (- (* x2 y1) (* x1 y2)) l1)]
      (fn [[x y]] [(+ (* a x) (* b y)) (- (* a y) (* b x))])))

(defn parse-mouse-event [ev]
  {:x (- (aget ev "pageX") 8)
   :y (- (aget ev "pageY") 8)
   :button (aget ev "button")})

(defn parse-keyboard-event [ev]
  {:key (char (aget ev "keyCode"))})


; Returns a function xform [x y] -> [X Y] that
; moves the point [x1 y1] to [x2 y2]
(defn translator-from-to [[x1 y1] [x2 y2]]
  (let [[vx vy] [(- x2 x1) (- y2 y1)]]
  (fn [[x y]] [(+ x )])))
