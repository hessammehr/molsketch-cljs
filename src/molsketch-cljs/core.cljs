(ns molsketch-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [molsketch-cljs.components :as cmp]
            [molsketch-cljs.constants :refer :all]
            [molsketch-cljs.util :refer :all]))

(enable-console-print!)

;(declare node add-node! node-click max-node-id node-position nearest-node
;  bond margin clip-line distance-squared distance canvas-click)


(def app-state (atom {:nodes
                      { 0 {:pos [10 15] :id 0 :elem :N}
                        1 {:pos [90 50] :id 1}
                        2 {:pos [90 120] :id 2 :elem :O}}
                      :bonds
                      { 0 {:nodes #{0 1} :id 1}
                        1 {:nodes #{1 2} :id 2}}}))

(defn node-click [node]
  (println "Node" node "clicked!"))

(defn canvas-click [ev]
  (let [x (- (aget ev "pageX") 8)
        y (- (aget ev "pageY") 8)
        nearest-node (nearest-node [x y] @app-state)
        nn-distance (distance (:pos nearest-node) [x y])]
      (if (> nn-distance node-click-radius)
        (add-node! {:pos [x y]})
        (node-click nearest-node))))

(defn margin [node])
  (if (:elem node) node-margin-radius 0)



(defn get-bonds [node {bonds :bonds}]
  (->> bonds
      seq
      (filter #(contains? (:nodes (second %)) node))
      (into {})))

; Number of bonds to node not counting implicit hydrogens
(defn explicit-bonds [node state]
  (count (get-bonds node state)))

(defn nearest-node [[x y] {nodes :nodes}]
  (->> nodes
      vals
      (apply min-key #(distance-squared [x y] (:pos %)))))

(defn max-node-id [{nodes :nodes}]
  (->> nodes
      keys
      (apply max)))

(defn add-node! [{id :id :as node
                  :or {id (+ (max-node-id @app-state) 1)}}]
      (swap! app-state assoc-in
        [:nodes id] (assoc node :id id)))

(defn editor []
  (let [{nodes :nodes bonds :bonds} @app-state]
    (conj [:svg {:on-click canvas-click}]
      (for [[id n] nodes]
        ^{:key id}[cmp/node n])
      (for [[id b] bonds]
          ^{:key id}[cmp/bond (update-in b [:nodes] (partial map nodes))]))))

;(println (draw))

(reagent/render-component [editor]
                          (. js/document (getElementById "app")))


(defn on-js-reload [])
