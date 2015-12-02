(ns molsketch-cljs.components
  (:require [molsketch-cljs.util :refer [clip-line]]
            [molsketch-cljs.constants :refer [node-radius margin]]))

(declare node bond)

(defn molecule [{nodes :nodes bonds :bonds} state] 1
  (concat
          (for [{id :id :as n} (map (:nodes state) nodes)]
            ^{:key (str "n" id)}[node n])
          (for [{id :id :as b} (map (:bonds state) bonds)]
            ^{:key (str "b" id)}[bond b state])))

(defn node [{[x y] :pos id :id elem :elem}]
  (if-not elem [:circle {:cx x :cy y :r node-radius :id id}]
                          ;:on-click (fn [ev] (node-click id))}]
               [:text {:x x :y y :id id
                       :class "label"} (name elem)]))

(defn bond [b state]
  (let [{n :nodes id :id} b
        [{p1 :pos} {p2 :pos} :as nodes] (map (:nodes state) n)
        [clip1 clip2] (map margin nodes)
        [[x1 y1] [x2 y2]] (clip-line p1 p2 clip1 clip2)]
   [:line {:x1 x1 :y1 y1
           :x2 x2 :y2 y2
           :id id
           :class "bond"}]))
