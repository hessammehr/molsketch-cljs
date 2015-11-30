(ns molsketch-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(declare node add-node! node-click max-node-id node-position nearest-node
  bond margin clip-line distance-squared distance canvas-click)

(def node-radius 3)
(def node-margin-radius 9)
(def node-click-radius 8)

(def elements {:N {:valence-electrons 5}})

(def app-state (atom {:nodes
                      { 0 {:pos [10 15] :id 0 :elem :N}
                        1 {:pos [90 50] :id 1}
                        2 {:pos [90 120] :id 2 :elem :O}}
                      :bonds
                      { 0 {:nodes #{0 1} :id 1}
                        1 {:nodes #{1 2} :id 2}}}))

(defn canvas-click [ev]
  (let [x (- (aget ev "pageX") 8)
        y (- (aget ev "pageY") 8)
        nearest-node (nearest-node [x y] @app-state)
        nn-distance (distance (:pos nearest-node) [x y])]
      (if (> nn-distance node-click-radius)
        (add-node! {:pos [x y]})
        (node-click nearest-node))))

(defn bond [{nodes :nodes id :id order :order}]
  (let [[{p1 :pos} {p2 :pos} :as nodes] (seq nodes)
        [clip1 clip2] (map margin nodes)
        [[x1 y1] [x2 y2]] (clip-line p1 p2 clip1 clip2)]
    [:line {:x1 x1 :y1 y1
            :x2 x2 :y2 y2
            :id id
            :class "bond"}]))

(defn margin [node]
  (if (:elem node) node-margin-radius 0))

(defn clip-line [[x1 y1] [x2 y2] clip1 clip2]
  (let [l (distance [x1 y1] [x2 y2])
        dx1 (/ (* clip1 (- x2 x1)) l)
        dx2 (/ (* clip2 (- x1 x2)) l)
        dy1 (/ (* clip1 (- y2 y1)) l)
        dy2 (/ (* clip2 (- y1 y2)) l)]
      [[(+ x1 dx1) (+ y1 dy1)] [(+ x2 dx2) (+ y2 dy2)]]))

(defn node [{[x y] :pos id :id elem :elem}]
  (if-not elem [:circle {:cx x :cy y :r node-radius :id (str "node" id)}]
                          ;:on-click (fn [ev] (node-click id))}]
               [:text {:x x :y y :id (str "node" id)
                       :class "label"} (name elem)]))

(defn node-click [node]
  (println "Node" node "clicked!"))

(defn get-bonds [node {bonds :bonds}]
  (->> bonds
      seq
      (filter #(contains? (:nodes (second %)) node))
      (into {})))

; Number of bonds to node not counting implicit hydrogens
(defn explicit-valence [node state]
  (count (get-bonds node state)))

(defn nearest-node [[x y] {nodes :nodes}]
  (->> nodes
      vals
      (apply min-key #(distance-squared [x y] (:pos %)))))

(defn max-node-id [{nodes :nodes}]
  (->> nodes
      keys
      (apply max)))

(defn node-position [node]
  (get-in node [:pos]))

(defn add-node! [{id :id :as node
                  :or {id (+ (max-node-id @app-state) 1)}}]
      (swap! app-state assoc-in
        [:nodes id] (assoc node :id id)))

(defn distance-squared [[x1 y1] [x2 y2]]
  (+ (Math/pow (- x1 x2) 2)
    (Math/pow (- y1 y2) 2)))
(defn distance [p1 p2]
  (Math.sqrt (distance-squared p1 p2)))

(defn editor []
  (let [{nodes :nodes bonds :bonds} @app-state]
    (conj [:svg {:on-click canvas-click}]
      (for [[id n] nodes]
        ^{:key id}[node n])
      (for [[id b] bonds]
          ^{:key id}[bond (update-in b [:nodes] (partial map nodes))]))))

;(println (draw))

(reagent/render-component [editor]
                          (. js/document (getElementById "app")))


(defn on-js-reload [])
