(ns molsketch-cljs.actions
  (:require [molsketch-cljs.functional :refer [sprout-bond delete active graft-at-node]]
            [molsketch-cljs.templates :refer [templates]]))
;           [molsketch-cljs.core :refer [app-state history]]))

(def keymap
  {\0
   (fn [{:keys [canvas]}]
     (when-let [[_ n-id] (active @canvas)]
       (swap! canvas sprout-bond n-id)))
   (char 46)
   (fn [{:keys [canvas]}]
     (when-let [[type id] (active @canvas)]
       (swap! canvas delete [type id])))
   \6
   (fn [{:keys [canvas]}]
     (when-let [[type id] (active @canvas)]
        (case type
              :nodes (swap! canvas graft-at-node (templates :cyclohexyl) id)
              :bonds (println "Not implemented!"))))})
