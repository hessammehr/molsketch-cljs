(ns molsketch-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [molsketch-cljs.components :as cmp]
            [molsketch-cljs.constants :refer [node-click-radius]]
            [molsketch-cljs.util :refer [distance clip-line max-node]]
            [molsketch-cljs.functional :refer [sprout-bond add-free-node
                                               add-molecule get-bonds]]))

(enable-console-print!)

(declare node node-click node-position nearest-node)
;  bond margin clip-line distance-squared distance canvas-click)


(def app-state
  (atom {:nodes
          { 0 {:pos [10 15] :id 0 :elem :P}
            1 {:pos [90 50] :id 1}
            2 {:pos [90 120] :id 2 :elem :O}}
         :bonds
          { 0 {:nodes #{0 1} :id 0}
            1 {:nodes #{1 2} :id 1}}
         :molecules
          { 0 {:nodes #{0 1 2} :bonds #{0 1} :id 0}}}))

(defn node-click [node]
  (println "Node" node "clicked!")
  ;(println (sprout-bond @app-state node))
  (swap! app-state sprout-bond node))

(defn canvas-click [ev]
  (let [x (- (aget ev "pageX") 8)
        y (- (aget ev "pageY") 8)
        nearest-node (nearest-node [x y] @app-state)
        nn-distance (distance (:pos nearest-node) [x y])]
      (if (> nn-distance node-click-radius)
        (swap! app-state add-free-node {:pos [x y]})
        (node-click nearest-node))))

; Number of bonds to node not counting implicit hydrogens
(defn explicit-bonds [node state]
  (count (get-bonds node state)))

(defn nearest-node [[x y] {nodes :nodes}]
  (->> nodes
      vals
      (apply min-key #(distance [x y] (:pos %)))))

(defn editor []
  (let [{molecules :molecules} @app-state]
    (into [:svg {:on-click canvas-click}]
      (for [[id m] molecules]
        ^{:key id}(cmp/molecule m @app-state)))))

(reagent/render-component [editor]
                          (. js/document (getElementById "app")))


(defn on-js-reload [])
