(ns molsketch-cljs.constants)

(def node-radius 2)
(def label-margin 9)
(def node-click-radius 8)
(def bond-length 30)
(def hover-distance 10)
(def min-drag-radius 10) ; movement before it's counted as a drag

(def elements {:N {:valence-electrons 5}})

(defn margin [node]
  (if (:elem node) label-margin node-radius))
