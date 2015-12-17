(ns molsketch-cljs.actions
  (:require [molsketch-cljs.functional :refer [sprout-bond delete-node
                                               active]]))

(def keymap
  {\0
   (fn [state-atom]
     (when-let [[_ n-id] (active @state-atom)]
       (swap! state-atom sprout-bond n-id)))
   (char 46)
   (fn [state-atom]
     (when-let [[_ n-id] (active @state-atom)]
       (swap! state-atom delete-node n-id)))})
