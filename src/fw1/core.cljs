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

; (defn hello-world []
;   [:h1 (:text @app-state)])

(defn draw []
  (let [{nodes :nodes bonds :bonds} @app-state]
    (conj [:svg]
      (for [[id {x :x y :y}] nodes]
        [:circle {:cx x :cy y
                  :r 3 :key id
                  :on-click (fn [] (js/alert (str "Hi from node " id)))}])
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
