(ns molsketch-cljs.fragment.xformations
   (:require-macros [com.rpl.specter :refer
                     [transform setval select]])
   (:require [com.rpl.specter :refer [ALL FIRST LAST VAL MAP-VALS]]
             [molsketch-cljs.fragment.query :refer [get-bonds]]))

(defn delete-bond [fragment bond-id]
  (update fragment :bonds dissoc bond-id))

(defn delete-node [fragment node-id]
  (let [bs (get-bonds fragment node-id)
        fragment (update fragment :nodes dissoc node-id)]
    (reduce delete-bond fragment bs)))

(defn remap [fragment node-mapping bond-mapping]
  "Changes the node and bond ids in fragment based (node-mapping <old-node-#>) and (bond-mapping <old-bond-#>)"
  (let [lookup {:nodes node-mapping :bonds bond-mapping}]
    (->> fragment
         (transform [:nodes ALL FIRST] node-mapping)
         (transform [:bonds MAP-VALS :nodes ALL] node-mapping)
         (transform [:bonds ALL FIRST] bond-mapping)
         (transform [:root :nodes] node-mapping)
         (transform [:root :bonds] bond-mapping))))

(defn transform [fragment xform]
  "Transforms node positions in fragment using (xform [x y]) -> [X Y]."
  (transform [:nodes MAP-VALS :pos] xform fragment))

(defn transform-cursor [fragment [type id] xform]
  "Transforms part of a fragment specified by a cursor,
  e.g., [:nodes 5] using (xform [x y]) -> [X Y]."
  (case type
    :nodes (update-in fragment [:nodes id :pos] xform)
    :bonds (reduce 
              (fn [frag n] (transform-cursor frag [:nodes n] xform))
              fragment 
              (get-in fragment [:bonds id :nodes]))))

(defn merge-fragments [f1 f2]
  "Merges f2 into f1"
  (-> f1
    (assoc :nodes (into (:nodes f1) (:nodes f2)))
    (assoc :bonds (into (:bonds f1) (:bonds f2)))))
