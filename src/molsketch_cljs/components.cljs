(ns molsketch-cljs.components
  (:require [molsketch-cljs.util :refer [clip-line]]
            [molsketch-cljs.constants :refer [node-radius margin
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

(defn bond [canvas b & {:keys [hovered selected]}]
  (let [{n :nodes} b
        [{p1 :pos} {p2 :pos} :as nodes] (map (:nodes canvas) n)
        [clip1 clip2] (map margin nodes)
        [[x1 y1] [x2 y2]] (clip-line p1 p2 clip1 clip2)]
    [:line {:x1 x1 :y1 y1
            :x2 x2 :y2 y2
            :class (str "bond"
                        (when hovered " hovered")
                        (when selected " selected"))}]))
