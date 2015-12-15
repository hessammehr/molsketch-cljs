(ns molsketch-cljs.actions
  (:require [molsketch-cljs.functional :refer [sprout-bond]]))

(def keymap
  {\0
   (fn [state]
     (if-let [[_ n-id] (get-in @state [:status :hovered])]
       (swap! state sprout-bond n-id)
       (when-let [[_ n-id] (get-in @state [:status :selected])]
         (swap! state sprout-bond n-id))))})
