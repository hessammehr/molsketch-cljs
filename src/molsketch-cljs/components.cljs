(ns molsketch-cljs.components
  (:require [molsketch-cljs.util :refer [clip-line]]))

(defn node [{[x y] :pos id :id elem :elem}]
  (if-not elem [:circle {:cx x :cy y :r node-radius :id (str "node" id)}]
                          ;:on-click (fn [ev] (node-click id))}]
               [:text {:x x :y y :id (str "node" id)
                       :class "label"} (name elem)]))

(defn bond [{nodes :nodes id :id order :order}]
 (let [[{p1 :pos} {p2 :pos} :as nodes] (seq nodes)
       [clip1 clip2] (map margin nodes)
       [[x1 y1] [x2 y2]] (clip-line p1 p2 clip1 clip2)]
   [:line {:x1 x1 :y1 y1
           :x2 x2 :y2 y2
           :id id
           :class "bond"}]))
