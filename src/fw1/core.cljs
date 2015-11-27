(ns fw1.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "Hey!")

;; define your app data so that it doesn't get over-written on reload

(def app-state (atom {:nodes
                      { 0 {:x 10 :y 15}
                        1 {:x 90 :y 50}}
                      :bonds
                      { 0 {:nodes [0 1]}}}))

(defn add-node [x y] (swap! app-state assoc-in
                      [:nodes (rand-int 1000)]
                      {:x x :y y}))

(defn handle-click [ev]
  (let [x (aget ev "clientX")
        y (aget ev "clientY")]
      (add-node x y)))

(defn draw []
  (let [{nodes :nodes bonds :bonds} @app-state]
    (conj [:svg {:on-click (fn [ev] (handle-click ev))}]
      (for [[id {x :x y :y}] nodes]
        [:circle {:cx x :cy y
                  :r 3 :key id}])
      (for [[id {[b1 b2] :nodes}] bonds]
        (let [n1 (get nodes b1) n2 (get nodes b2)]
          [:line {:x1 (:x n1)
                  :y1 (:y n1)
                  :x2 (:x n2)
                  :y2 (:y n2)
                  :class "bond"
                  :key id}])))))


(println (draw))

(reagent/render-component [draw]
                          (. js/document (getElementById "app")))


(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
