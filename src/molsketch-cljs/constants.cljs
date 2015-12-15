(ns molsketch-cljs.constants
  (:require [molsketch-cljs.actions :refer
             [sprout-from-hovered-or-selected]]))

(def node-radius 2)
(def label-margin 9)
(def node-click-radius 5)
(def bond-length 30)
(def hover-radius 5)
(def min-drag-radius 5) ; movement before it's counted as a drag
(def fuse-tolerance 5)

(def editor-dimensions [600 200])

(def elements {:N {:valence-electrons 5}})

(defn margin [node]
  (if (:elem node) label-margin node-radius))

(def keymap {\0 #(sprout-from-hovered-or-selected)})

;; (def templates {:methyl {:nodes { 0 }}})
