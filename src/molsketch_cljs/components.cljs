(ns molsketch-cljs.components
  (:require [molsketch-cljs.util :refer [clip-line]]
            [molsketch-cljs.constants :refer [node-radius margin
                                              hover-marker-radius
                                              selection-marker-radius]]))

(declare node bond hover-marker selection-marker)

;; (defn molecule [state {nodes :nodes bonds :bonds}]
;;   (concat
;;           (for [{id :id :as n} (map (:nodes state) nodes)]
;;             ^{:key (str "n" id)}[node state n])
;;           (for [{id :id :as b} (map (:bonds state) bonds)]
;;             ^{:key (str "b" id)}[bond state b])))

(defn molecule [state mol]
  [:g {}
   (for [[id n] (:nodes state)]
     ^{:key (str "n" id)}[node state n])
   (for [[id b] (:bonds state)]
     ^{:key (str "b" id)}[bond state b])
   [hover-marker state]
   [selection-marker state]])

(defn hover-marker [state]
  (when-let [[type id] (get-in state [:status :hovered])]
    (case type
      :nodes (let [[x y] (get-in state [type id :pos])]
                  [:circle {:cx x :cy y :r hover-marker-radius :class "hover-marker"}])
      :bonds [bond state (get-in state [type id]) :hovered true])))

(defn selection-marker [state]
  (when-let [[type id] (get-in state [:status :selected])]
    (case type
      :nodes (let [[x y] (get-in state [type id :pos])]
                  [:circle {:cx x :cy y :r selection-marker-radius :class "selection-marker"}])
      :bonds [bond state (get-in state [type id]) :selected true])))

(defn node [state n]
  (let [{[x y] :pos elem :elem class :class} n]
    (if-not elem [:circle {:cx x :cy y :r node-radius :class class}]
           [:text {:x x :y y :class (str "label " class)}
            (name elem)])))

(defn bond [state b & {:keys [hovered selected]}]
  (let [{n :nodes} b
        [{p1 :pos} {p2 :pos} :as nodes] (map (:nodes state) n)
        [clip1 clip2] (map margin nodes)
        [[x1 y1] [x2 y2]] (clip-line p1 p2 clip1 clip2)]
    [:line {:x1 x1 :y1 y1
            :x2 x2 :y2 y2
            :class (str "bond"
                        (when hovered " hovered")
                        (when selected " selected"))}]))
