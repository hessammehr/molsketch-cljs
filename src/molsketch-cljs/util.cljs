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

(defn invert [v]
  (mapv - v))

(defn angle [[x y]]
  (let [y (- y)
        a (Math/atan (/ y x))
        a (if (pos? x) a (+ a (.-PI js/Math)))]
    (/ (* a 180) (.-PI js/Math))))

(defn rotate
  ([[x y] degrees]
   (let [t (/ (* degrees (.-PI js/Math)) 180)]
     [(- (* x (Math/cos t)) (* y (Math/sin t)))
      (+ (* x (Math/sin t)) (* y (Math/cos t)))]))
  ([[x y] degrees [cx cy]]
   (mapv + [cx cy]
     (rotate [(- x cx) (- y cy)] degrees))))

#_(defn unitary-xform
   [[x y] a & {:keys [[ox oy] :as origin sign] :or {origin [0 0] sign 1}}]
   (let [A (* (Math/sign sign) (Math/sqrt (- 1 (* a a))))
         [X Y] (mapv - [x y] origin)]
     (mapv + origin
       [(+ (* X A) (* Y (- a)))
        (+ (* X a) (* Y A))])))

; Matrix multiply with sqrt(1-a^2)     -a
;                           a       sqrt(1-a^2)
; representing a general rotation smaller than >-90 and <90 degrees
(defn rotation-like
  ([[x y] a]
   (let [A (Math/sqrt (- 1 (* a a)))]
     [(+ (* x A) (* y (- a)))
      (+ (* x a) (* y A))]))
  ([[x y] a origin]
   (let [[ox oy] origin]
     (mapv + origin
       (rotation-like [(- x ox) (- y oy)] a)))))

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

; (defn map-in [coll loc mapping]
;   (update-in coll loc (fn [v] (into (empty v)
;                                     (map mapping v)))))

; (defn dissoc-in
;   "Dissociates an entry from a nested associative structure returning a new
;   nested structure. keys is a sequence of keys. Any empty maps that result
;   will not be present in the new structure."
;   [m [k & ks :as keys]]
;   (if ks
;     (if-let [nextmap (get m k)]
;       (let [newmap (dissoc-in nextmap ks)]
;         (if (seq newmap)
;           (assoc m k newmap)
;           (dissoc m k)))
;       m)
;    (dissoc m k)))

; Returns a unitary transformation that orients
; [x1 y1] along [x2 y2] by rotation and scaling
(defn rotator-from-to [[x1 y1] [x2 y2]]
  (let [l1 (+ (* x1 x1) (* y1 y1))
        l2 (+ (* x2 x2) (* y2 y2))
        [x2 y2] (map #(* % (Math/sqrt (/ l1 l2)))
                 [x2 y2]) ; rescale [x2 y2] to be the same length as [x1 y1]
        a (/ (+ (* x1 x2) (* y1 y2)) l1)
        b (/ (- (* x2 y1) (* x1 y2)) l1)]
      (fn [[x y]] [(+ (* a x) (* b y)) (- (* a y) (* b x))])))

; Returns a function xform [x y] -> [X Y] that
; moves the point [x1 y1] to [x2 y2]
(defn translator-from-to [[x1 y1] [x2 y2]]
  (let [[vx vy] [(- x2 x1) (- y2 y1)]])
  (fn [[x y]] [(+ x vx) (+ y vy)]))
