(ns molsketch-cljs.actions
  (:require [molsketch-cljs.functional :refer [sprout-bond delete active graft-at-node]]
            [molsketch-cljs.templates :refer [templates]]))
;           [molsketch-cljs.core :refer [app-state history]]))

(def keymap
  {\0
   (fn [{:keys [canvas status]}]
     (when-let [[_ n-id] (active @status)]
       (swap! canvas sprout-bond n-id)))
   (char 46)
   (fn [{:keys [canvas status]}]
     (when-let [[type id] (active @status)]
       (swap! canvas delete [type id])))
   \6
   (fn [{:keys [canvas status]}]
     (when-let [[type id] (active @status)]
        (case type
              :nodes (swap! canvas graft-at-node (templates :cyclohexyl) id)
              :bonds (println "Not implemented!"))))
   \Z
   (fn [{:keys [canvas history]}]
    (when-let [s (peek @history)]
      (let [ss (pop @history)]
        (reset! canvas s)
        (reset! history ss))))})
