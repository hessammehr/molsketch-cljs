(ns molsketch-cljs.components
  (:require [molsketch-cljs.util :refer [clip-line displacement rotate-degrees normalize]]
            [molsketch-cljs.constants :refer [node-radius margin multiple-bond-spacing
                                              node-marker-radius]]))
                                              
(declare node bond hover-marker selection-marker)

(defn structure [canvas status]
  [:g {}
   (for [[id n] (:nodes canvas)]
     ^{:key (str "n" id)}[node canvas n])
   (for [[id b] (:bonds canvas)]
     ^{:key (str "b" id)}[bond canvas b])
   [hover-marker canvas status]
   [selection-marker canvas status]])
   
(defn hover-marker [canvas status]
  (when-let [[type id] (:hovered status)]
      [(case type :nodes node :bonds bond) canvas 
       (get-in canvas [type id]) :hovered true]))
      
(defn selection-marker [canvas status]
  (when-let [[type id] (:selected status)]
    [(case type :nodes node :bonds bond) canvas
     (get-in canvas [type id]) :selected true]))

(defn node [canvas n & {:keys [hovered selected]}]
  (let [{[x y] :pos elem :elem} n
        cls (cond hovered "hovered" selected "selected")]
    (if cls [:circle {:cx x :cy y :r node-marker-radius :class cls}]
        (if-not elem [:circle {:cx x :cy y :r node-radius}]
               [:text {:x x :y y :class (str "label" cls)}
                (name elem)]))))

; (defn multiple-bond [[x1 y1] [x2 y2]]
;   (let [bond-vec (displacement [x1 y1] [x2 y2])
;         offset-vec ]))

(defn bond [canvas b & {:keys [hovered selected]}]
  (let [{n :nodes order :order :or {order 1}} b
        [{p1 :pos} {p2 :pos} :as nodes] (map (:nodes canvas) n)
        [clip1 clip2] (map margin nodes)
        [[x1 y1] [x2 y2]] (clip-line p1 p2 clip1 clip2)
        offset (- (* (- order 1) (/ multiple-bond-spacing 2)))
        offset-dir (rotate-degrees (displacement p1 p2) 90.0)
        [offset-x offset-y] (normalize offset-dir offset)
        [delta-x delta-y] (normalize offset-dir multiple-bond-spacing)]
    [:g  (for [n (range order)]
          ^{:key (str "bline" n)}
          [:line {:x1 (+ x1 offset-x (* n delta-x)) :y1 (+ y1 offset-y (* n delta-y))
                  :x2 (+ x2 offset-x (* n delta-x)) :y2 (+ y2 offset-y (* n delta-y))
                  :class (str "bond"
                            (when hovered " hovered")
                            (when selected " selected"))}])]))
