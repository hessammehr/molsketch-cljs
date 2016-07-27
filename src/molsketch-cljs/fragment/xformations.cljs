(ns molsketch-cljs.fragment.xformations)


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
