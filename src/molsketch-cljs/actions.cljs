(ns molsketch-cljs.actions
  (:require [molsketch-cljs.functional :refer [sprout-bond delete-node
                                               active graft]]
            [molsketch-cljs.templates :refer [templates]]))

(def keymap
  {\0
   (fn [state-atom]
     (when-let [[_ n-id] (active @state-atom)]
       (swap! state-atom sprout-bond n-id)))
   (char 46)
   (fn [state-atom]
     (when-let [[_ n-id] (active @state-atom)]
       (swap! state-atom delete-node n-id)))
   \6
   (fn [state-atom]
     (when-let [at (active @state-atom)]
       (swap! state-atom graft (templates :cyclohexyl) at)))})
