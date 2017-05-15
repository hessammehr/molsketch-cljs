(ns molsketch-cljs.actions
  (:require [molsketch-cljs.functional :refer [active]]
            [molsketch-cljs.fragment.xformations :refer 
             [delete sprout-bond sprout-at-node graft-at-node]]
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
   \5
   (fn [{:keys [canvas status]}]
     (when-let [[type id] (active @status)]
       (case type
         :nodes (swap! canvas graft-at-node (templates :cyclopentyl) id)
         :bonds (println "Not implemented!"))))
   \4
   (fn [{:keys [canvas status]}]
     (when-let [[type id] (active @status)]
       (case type
         :nodes (swap! canvas graft-at-node (templates :cyclobutyl) id)
         :bonds (println "Not implemented!"))))
   \3
   (fn [{:keys [canvas status]}]
     (when-let [[type id] (active @status)]
       (case type
         :nodes (swap! canvas graft-at-node (templates :cyclopropyl) id)
         :bonds (swap! canvas assoc-in [:bonds id :order] 3))))
   \2
   (fn [{:keys [canvas status]}]
     (when-let [[type id] (active @status)]
       (case type
         :nodes (println "Not implemented!")
         :bonds (swap! canvas assoc-in [:bonds id :order] 2))))
   \1
   (fn [{:keys [canvas status]}]
     (when-let [[type id] (active @status)]
       (case type
         :nodes (println "Not implemented!")
         :bonds (swap! canvas assoc-in [:bonds id :order] 1))))

   :ctrl
   {\Z
    (fn [{:keys [canvas history]}]
      (when-let [s (peek @history)]
        (let [ss (pop @history)]
          (reset! canvas s)
          (reset! history ss))))}

   :shift
   {\6
    (fn [{:keys [canvas status]}]
      (when-let [[type id] (active @status)]
        (case type
          :nodes (swap! canvas sprout-at-node (templates :cyclohexyl) id)
          :bonds (println "Not implemented!"))))
   \5
   (fn [{:keys [canvas status]}]
     (when-let [[type id] (active @status)]
       (case type
         :nodes (swap! canvas sprout-at-node (templates :cyclopentyl) id)
         :bonds (println "Not implemented!"))))
   \4
   (fn [{:keys [canvas status]}]
     (when-let [[type id] (active @status)]
       (case type
         :nodes (swap! canvas sprout-at-node (templates :cyclobutyl) id)
         :bonds (println "Not implemented!"))))
   \3
   (fn [{:keys [canvas status]}]
     (when-let [[type id] (active @status)]
       (case type
         :nodes (swap! canvas sprout-at-node (templates :cyclopropyl) id)
         :bonds (println "Not implemented!"))))}
   })
