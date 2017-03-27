(ns molsketch-cljs.core
  (:require [reagent.core :as reagent :refer [atom]]
            [molsketch-cljs.components :refer [structure]]
            [molsketch-cljs.constants :refer [click-radius hover-radius
                                              min-drag-radius
                                              editor-dimensions]]
            [molsketch-cljs.events :refer [parse-mouse-event parse-keyboard-event]]
            [molsketch-cljs.util :refer [distance clip-line translator-from-to]]
            [molsketch-cljs.functional] 
            [molsketch-cljs.fragment.xformations
              :refer [transform-cursor]]
            [molsketch-cljs.fragment.query
              :refer [nodes-within node-inside bond-inside get-bonds]]
            [molsketch-cljs.actions :refer [keymap]]))

(enable-console-print!)

(def blank-canvas
  {:nodes
   {0 {:pos [65 30] :elem :P}
    1 {:pos [90 50]}
    2 {:pos [90 80] :elem :O}}
   :bonds
   {0 {:nodes #{0 1}}
    1 {:nodes #{1 2}}}})
   
(def app-state
  {:canvas (atom blank-canvas)
   :status (atom {:mode :normal :mouse nil :key-seq []})
   :history (atom [])})

(add-watch (:canvas app-state) :canvas
 (fn [_ _ old new]
  (when-not 
    false ;(get-in @(:status app-state) [:mouse :dragging])
    (swap! (:history app-state) conj old))))

(defn normal-mouse-move [{x :x y :y}]
  (let [{:keys [canvas status]} app-state
        n (node-inside @canvas [x y] hover-radius)
        b (bond-inside @canvas [x y] hover-radius)
        s (or (when n [:nodes n]) (when b [:bonds b]))]
    (swap! status assoc :hovered s)))

(defn normal-click [{x :x y :y}]
  (let [{:keys [canvas status]} app-state
        n (node-inside @canvas [x y] click-radius)
        b (bond-inside @canvas [x y] click-radius)
        s (or (when n [:nodes n]) (when b [:bonds b]))]
    (swap! status assoc :selected s))) 
      

(defn do-drag [{x2 :x y2 :y}]
  (let [{:keys [canvas status]} app-state
        cursor (get @status :hovered)
        {x1 :x y1 :y} (get @status :mouse)
        xform (translator-from-to [x1 y1] [x2 y2])]
    (swap! status update :mouse merge {:x x2 :y y2 :dragging true})
    (swap! canvas transform-cursor cursor xform)))

(defn end-drag [{x :x y :y}]
  (println "Drag ended: " x y))

(defn mouse-down [ev]
  (let [{:keys [x y] :as parsed} (parse-mouse-event ev)
        {status :status} app-state]
    (swap! status assoc :mouse parsed)))

(defn mouse-up [ev]
  (let [{status :status} app-state
        {x2 :x y2 :y} (parse-mouse-event ev)
        {x1 :x y1 :y} (get @status :mouse)]
    (if (get-in @status [:mouse :dragging])
      (end-drag {:x x2 :y y2})
      (normal-click {:x x1 :y y1}))
    (swap! status assoc :mouse nil)))

(defn mouse-move [ev]
  (let [{x :x y :y :as parsed} (parse-mouse-event ev)
        {status :status} app-state]
    (if-not (get @status :mouse)
      (normal-mouse-move parsed)
      (do-drag parsed))))

(defn key-press [ev]
  (let [{k :key} (parse-keyboard-event ev)
        {status :status} app-state
        key-seq (conj (get @status :key-seq []) k)
        f (get-in keymap key-seq)]
    (println key-seq f)
    (cond
      (nil? f) (swap! status assoc :key-seq [])
      (map? f) (swap! status assoc [:status :key-seq] key-seq)
      :else (do (f app-state)
                (swap! status assoc :key-seq [])))))

(defn editor []
  (let [{:keys [canvas status history]} app-state]
    [:div.editor {:on-key-down key-press}
      [:svg 
        {:on-mouse-up mouse-up :on-mouse-move mouse-move :on-mouse-down mouse-down}
        [structure @canvas @status]]
      [:p.status (str "Canvas: " @canvas)]
      [:p.status (str "Status: " @status)]
      [:p.status (str "History: (" (count @history) " items)")]]))

(reagent/render-component [editor]
                          (. js/document (getElementById "app")))

(aset js/document "onkeydown" key-press)

(defn on-js-reload []
  (println "Reloaded!"))
