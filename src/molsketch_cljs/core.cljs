(ns molsketch-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [molsketch-cljs.components :as cmp]
            [molsketch-cljs.constants :refer [click-radius hover-radius
                                              min-drag-radius
                                              editor-dimensions]]
            [molsketch-cljs.events :refer [parse-mouse-event parse-keyboard-event]]
            [molsketch-cljs.util :refer [distance clip-line translator-from-to]]
            [molsketch-cljs.functional
              :refer [add-free-node add-molecule]] 
            [molsketch-cljs.fragment.xformations
              :refer [transform-cursor]]
            [molsketch-cljs.fragment.query
              :refer [nodes-within node-inside bond-inside get-bonds]]
            [molsketch-cljs.actions :refer [keymap]]))

(enable-console-print!)

(declare node-click)

(def blank-state
  {:nodes
   {0 {:pos [65 30] :elem :P}
    1 {:pos [90 50]}
    2 {:pos [90 80] :elem :O}}
   :bonds
   {0 {:nodes #{0 1}}
    1 {:nodes #{1 2}}}
   :molecules
   {0 {:nodes #{0 1 2} :bonds #{0 1}}}
   :status
   {:mode :normal :mouse nil :key-seq []}})

(defonce app-state
  (atom blank-state))

(defn normal-mouse-move [{x :x y :y}]
  (let [n (node-inside @app-state [x y] hover-radius)
        b (bond-inside @app-state [x y] hover-radius)]
    (swap! app-state assoc-in [:status :hovered] (if n [:nodes n] (when b [:bonds b])))))

(defn normal-click [{x :x y :y}]
  (let [n (node-inside @app-state [x y] click-radius)
        b (bond-inside @app-state [x y] click-radius)]
    (swap! app-state assoc-in [:status :selected] (if n [:nodes n] (when b [:bonds b])))))

(defn do-drag [{x2 :x y2 :y}]
  (let [cursor (get-in @app-state [:status :hovered])
        {x1 :x y1 :y} (get-in @app-state [:status :mouse])
        xform (translator-from-to [x1 y1] [x2 y2])]
    (swap! app-state update-in [:status :mouse] merge {:x x2 :y y2 :dragging true})
    (swap! app-state transform-cursor cursor xform)))

(defn end-drag [{x :x y :y}]
  (println "Drag ended: " x y))

(defn mouse-down [ev]
  (let [{:keys [x y] :as parsed} (parse-mouse-event ev)]
    (swap! app-state assoc-in [:status :mouse] parsed)))

(defn mouse-up [ev]
  (let [{x2 :x y2 :y} (parse-mouse-event ev)
        {x1 :x y1 :y} (get-in @app-state [:status :mouse])]
    (if (get-in @app-state [:status :mouse :dragging])
      (end-drag {:x x2 :y y2})
      (normal-click {:x x1 :y y1}))
    (swap! app-state assoc-in [:status :mouse] nil)))

(defn mouse-move [ev]
  (let [{x :x y :y :as parsed} (parse-mouse-event ev)]
    (if (= (get-in @app-state [:status :mouse]) nil)
      (normal-mouse-move parsed)
      (do-drag parsed))))

(defn key-press [ev]
  (let [{k :key} (parse-keyboard-event ev)
        key-seq (conj (get-in @app-state [:status :key-seq]) k)
        f (get-in keymap key-seq)]
    (cond
      (nil? f) (swap! app-state assoc-in [:status :key-seq] [])
      (map? f) (swap! app-state assoc-in [:status :key-seq] key-seq)
      :else (do (f app-state)
                (swap! app-state assoc-in [:status :key-seq] [])))))

(defn editor []
  (let [{molecules :molecules :as state} @app-state]
    [:div.editor {:on-key-down key-press}
      [:svg {:on-mouse-up mouse-up :on-mouse-move mouse-move
             :on-mouse-down mouse-down}
       (for [[id m] molecules]
         ^{:key (str "m" id)}[cmp/molecule state m])]
      [:p.status (str state)]]))

(reagent/render-component [editor]
                          (. js/document (getElementById "app")))

(aset js/document "onkeydown" key-press)

(defn on-js-reload []
  (println "Reloaded!"))
