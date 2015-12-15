(ns molsketch-cljs.actions
  (:require [molsketch-cljs.functional :refer [sprout-bond
                                               active]]))

(def keymap
  {\0
   (fn [state-atom]
     (when-let [[_ n-id] (active @state-atom)]
       (swap! state-atom sprout-bond n-id)))
   })
