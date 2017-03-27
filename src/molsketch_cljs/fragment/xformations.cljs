(ns molsketch-cljs.fragment.xformations
   (:require-macros [com.rpl.specter :refer
                     [transform setval select]])
   (:require [com.rpl.specter :refer [ALL FIRST LAST VAL MAP-VALS]]
             [molsketch-cljs.fragment.query :refer
              [get-bonds max-node max-bond fusion-candidates sprout-position]]))

(defn delete-bond [fragment bond-id]
  (update fragment :bonds dissoc bond-id))

(defn delete-node [fragment node-id]
  (let [bs (get-bonds fragment node-id)
        fragment (update fragment :nodes dissoc node-id)]
    (reduce delete-bond fragment bs)))

(defn delete [state [type id]]
  (let [s (assoc-in state [:status :hovered] nil)]
       (case type
             :nodes (delete-node s id)
             :bonds (delete-bond s id))))

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

(defn new-node [fragment node-props]
  (let [id (inc (max-node fragment))]
    [id node-props]))

(defn add-node [fragment [n-id node-props]]
  (-> fragment
      (assoc-in [:nodes n-id] node-props)))

(defn new-bond [fragment bond-props]
  "Returns a new hashmap representing a bond produced by associng an
  auto-incremented :id to the bond-propes provided."
  [(inc (max-bond fragment)) bond-props])

(defn add-bond [fragment [b-id b-props]]
  "Appends a bond to given fragment."
  (-> fragment
      (assoc-in [:bonds b-id] b-props)))
      ;(update-in [:molecules mol-id :bonds] conj (:id b))))

(defn connect [fragment n1-id n2-id]
  "Connects nodes #'s n1-id and n2-id."
  (let [b (new-bond fragment {:nodes #{n1-id n2-id}})]
    (-> fragment
        (add-bond b))))

(defn sprout-bond [fragment node-id]
  (if-let [c (first (fusion-candidates fragment node-id))]
    (connect fragment node-id c)
    (let [new-pos (sprout-position fragment node-id)
          n (new-node fragment {:pos new-pos})
          b (new-bond fragment {:nodes #{node-id (first n)}})]
      (-> fragment
          (add-node n)
          (add-bond b)))))

