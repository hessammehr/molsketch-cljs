(ns molsketch-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [molsketch-cljs.components :as cmp]
            [molsketch-cljs.constants :refer [node-click-radius hover-distance
                                              min-drag-radius]]
            [molsketch-cljs.util :refer [distance clip-line max-node
                                         parse-mouse-event]]
            [molsketch-cljs.functional
              :refer [sprout-bond add-free-node add-molecule
                      get-bonds nearest-node prepare]]))

(enable-console-print!)

(declare node-click)
;  bond margin clip-line distance-squared distance canvas-click)


(def app-state
  (atom {:nodes
          { 0 {:pos [65 30] :id 0 :elem :P}
            1 {:pos [90 50] :id 1}
            2 {:pos [90 80] :id 2 :elem :O}}
         :bonds
          { 0 {:nodes #{0 1} :id 0}
            1 {:nodes #{1 2} :id 1}}
         :molecules
          { 0 {:nodes #{0 1 2} :bonds #{0 1} :id 0}}
         :status [:normal]}))

(defn node-click [node]
  (swap! app-state sprout-bond node))
  ;(println (sprout-bond @app-state (:id node))))

(defn normal-mouse-move [{x :x y :y}]
  (let [[nearest nn-distance] (nearest-node @app-state [x y])]
    (swap! app-state assoc :hovered
      (when (< nn-distance hover-distance) [:nodes nearest]))))
    ;(println (str "Nearest node: ", nearest))))

(defn normal-click [{x :x y :y}]
  (let [[nearest nn-distance] (nearest-node @app-state [x y])]
      (if (> nn-distance node-click-radius)
        (swap! app-state add-free-node {:pos [x y]})
        (node-click nearest))))

(defn end-drag [{x :x y :y}]
  (println "Drag ended: " x y))

(defn mouse-down [ev]
  (let [{x :x y :y} (parse-mouse-event ev)]
    (swap! app-state assoc :status [:down {:x x :y y}])))

(defn mouse-up [ev]
  (let [{x2 :x y2 :y} (parse-mouse-event ev)
        {x1 :x y1 :y} (get-in @app-state [:status 1])]
    (if (< (distance [x1 y1] [x2 y2]) min-drag-radius)
        (normal-click {:x x1 :y y1})
        (end-drag {:x x2 :y y2}))
    (swap! app-state assoc :status [:normal])))

(defn mouse-move [ev]
  (let [{x :x y :y} (parse-mouse-event ev)]
    (if (= (:status @app-state) [:normal])
      (normal-mouse-move (parse-mouse-event ev))
      (println "Dragging: " x y))))

(defn editor []
  (let [{molecules :molecules :as state} (prepare @app-state)]
    [:svg {:on-mouse-up mouse-up :on-mouse-move mouse-move
           :on-mouse-down mouse-down}
      (doall (for [[id m] molecules]
              ^{:key (str "m" id)}(cmp/molecule state m)))]))

(reagent/render-component [editor]
                          (. js/document (getElementById "app")))


(defn on-js-reload [])
