(ns molsketch-cljs.constants)

(def node-radius 0.5)
(def label-margin 8)
(def click-radius 5)
(def bond-length 30)
(def hover-radius 5)
(def min-drag-radius 5) ; movement before it's counted as a drag
(def fuse-tolerance 5)
(def node-marker-radius 5)
(def multiple-bond-spacing 3)

(def editor-dimensions [600 200])

(def elements {:N {:valence-electrons 5}})

(defn margin [node]
  (if (:elem node) label-margin node-radius))

(def mouse-offset-horizontal 8)
(def mouse-offset-vertical 8)
