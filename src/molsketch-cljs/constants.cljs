(ns molsketch-cljs.constants)

(def node-radius 3)
(def label-margin 9)
(def node-click-radius 8)
(def bond-length 25)

(def elements {:N {:valence-electrons 5}})

(defn margin [node]
  (if (:elem node) label-margin node-radius))
