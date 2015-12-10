(ns molsketch-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [molsketch-cljs.components :as cmp]
            [molsketch-cljs.constants :refer [node-click-radius hover-radius
                                              min-drag-radius editor-dimensions]]
            [molsketch-cljs.util :refer [distance clip-line max-node
                                         parse-mouse-event]]
            [molsketch-cljs.functional
              :refer [sprout-bond add-free-node add-molecule
                      get-bonds node-within prepare]]))

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
         :status {:mode :normal :mouse nil}}))

(defn node-click [node-id]
  (swap! app-state sprout-bond node-id))
  ;(println (sprout-bond @app-state (:id node))))

(defn normal-mouse-move [{x :x y :y}]
  (let [n (node-within @app-state [x y] hover-radius)]
    (println x y)
    (swap! app-state assoc-in [:status :hovered] (when n [:nodes n]))))

(defn normal-click [{x :x y :y}]
  (if-let [n (node-within @app-state [x y] node-click-radius)]
    (node-click n)
    (swap! app-state add-free-node {:pos [x y]})))

(defn do-drag [{x :x y :y}]
  (let [h (get-in @app-state [:status :hovered])]
    (swap! app-state update-in h assoc :pos [x y])))

(defn end-drag [{x :x y :y}]
  (println "Drag ended: " x y))

(defn mouse-down [ev]
  (let [{:keys [x y] :as parsed} (parse-mouse-event ev)]
    (swap! app-state assoc-in [:status :mouse] parsed)))

(defn mouse-up [ev]
  (let [{x2 :x y2 :y} (parse-mouse-event ev)
        {x1 :x y1 :y} (get-in @app-state [:status :mouse])]
    (if (< (distance [x1 y1] [x2 y2]) min-drag-radius)
        (normal-click {:x x1 :y y1})
        (end-drag {:x x2 :y y2}))
    (swap! app-state assoc-in [:status :mouse] nil)))

(defn mouse-move [ev]
  (let [{x :x y :y :as parsed} (parse-mouse-event ev)]
    (if (= (get-in @app-state [:status :mouse]) nil)
      (normal-mouse-move parsed)
      (do-drag parsed))))

(defn editor []
  (let [{molecules :molecules :as state} (prepare @app-state)]
    [:div.editor
      [:svg {:on-mouse-up mouse-up :on-mouse-move mouse-move
             :on-mouse-down mouse-down}
       (doall (for [[id m] molecules]
                ^{:key (str "m" id)}(cmp/molecule state m)))]
      [:p (str (:status state))]]))

(reagent/render-component [editor]
                          (. js/document (getElementById "app")))


(defn on-js-reload [])
