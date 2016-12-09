(ns molsketch-cljs.actions
  (:require [molsketch-cljs.functional :refer [sprout-bond delete active graft-at-node]]
            [molsketch-cljs.templates :refer [templates]]))

(def keymap
  {\0
   (fn [state-atom]
     (when-let [[_ n-id] (active @state-atom)]
       (swap! state-atom sprout-bond n-id)))
   (char 46)
   (fn [state-atom]
     (when-let [[type id] (active @state-atom)]
       (swap! state-atom delete [type id])))
   \6
   (fn [state-atom]
     (when-let [[type id] (active @state-atom)]
        (case type
              :nodes (swap! state-atom graft-at-node (templates :cyclohexyl) id)
              :bonds (println "Not implemented!"))))})
