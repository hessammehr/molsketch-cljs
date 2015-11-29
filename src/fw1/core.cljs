(ns fw1.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(declare max-node-id distance-squared node-position nearest-node
  distance-squared distance node-click)

(def node-radius 3)
(def node-click-radius 8)

(def app-state (atom {:nodes
                      { 0 {:pos [10 15] :id 0}
                        1 {:pos [90 50] :id 1}
                        2 {:pos [90 120] :id 2}}
                      :bonds
                      { 0 {:nodes #{0 1} :id 1}
                        1 {:nodes #{1 2} :id 2}}}))

(defn max-node-id []
  (->> (:nodes @app-state)
      keys
      (apply max)))

(defn node-position [node]
  (get-in node [:pos]))

(defn add-node! [{id :id :as node :or {id (+ (max-node-id) 1)}}]
  ; (let ;[new-id (+ (max-node-id) 1)
  ;       node
      (swap! app-state assoc-in
        [:nodes id] node))

(defn canvas-click [ev]
  (let [x (- (aget ev "pageX") 8)
        y (- (aget ev "pageY") 8)
        nearest-node (nearest-node [x y])
        nn-distance (distance (:pos nearest-node) [x y])]
      (if (> nn-distance node-click-radius)
        (add-node! {:pos [x y]})
        (node-click nearest-node))))

(defn node-click [node]
  (println "Node" node "clicked!"))

(defn bond [{[x1 y1] :pos} {[x2 y2] :pos}]
    [:line {:x1 x1 :y1 y1
            :x2 x2 :y2 y2
            :class "bond"}])

(defn node [{[x y] :pos id :id}]
  [:circle {:cx x :cy y :r node-radius
            :on-click (fn [ev] (node-click id))}])

(defn get-bonds [node]
  (->> (:bonds @app-state)
      seq
      (filter #(contains? (:nodes (second %)) node))
      (into {})))

; Number of bonds to node not counting implicit hydrogens
(defn explicit-valence [node]
  (count (get-bonds node)))

(defn nearest-node [[x y]]
  (->> (:nodes @app-state)
      vals
      (apply min-key #(distance-squared [x y] (:pos %)))))

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
      (for [[id {n :nodes}] bonds
            :let [[n1 n2] (map nodes (seq n))]]
          ^{:key id}[bond n1 n2]))))

;(println (draw))

(reagent/render-component [editor]
                          (. js/document (getElementById "app")))


(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
