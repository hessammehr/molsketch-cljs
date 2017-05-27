(ns molsketch-cljs.fragment.xformations
   (:require-macros [com.rpl.specter :refer
                     [transform setval select]])
   (:require [com.rpl.specter :refer [ALL FIRST LAST VAL MAP-VALS]]
             [molsketch-cljs.fragment.query :refer
              [get-bonds max-node max-bond fusion-candidates
               sprout-position sprout-direction connected]]
             [molsketch-cljs.util :refer [xform-from-to translator-from-to]]))

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

(defn transform-nodes [fragment xform]
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

(defn graft-at-node [fragment1 fragment2 node]
  "Graft fragment2 onto fragment1 at node n-id. The root node(s) of 
  fragment2 are bonded to n-id."
  (let [min-node-id (inc (max-node fragment1))
        min-bond-id (inc (max-bond fragment1))
        node-mapping (partial + min-node-id)
        bond-mapping (partial + min-bond-id)
        root-id (get-in fragment2 [:root :nodes])
        root-pos (get-in fragment2 [:nodes root-id :pos])
        graft-pos (get-in fragment1 [:nodes node :pos])
        graft-dir (sprout-direction fragment1 node)
        xform (xform-from-to root-pos graft-dir)
        translation (translator-from-to (xform root-pos) graft-pos)
        root-neighbours (connected fragment2 root-id) ; nodes connected to root
        fragment2 (-> fragment2
                   (dissoc :root)
                   (delete [:nodes root-id])
                   (transform-nodes (comp translation xform))
                   (remap node-mapping bond-mapping))
        fragment1 (-> fragment1
                      (merge-fragments fragment2))]
    ; graft root's neighbours at graft position
    (reduce (fn [frag n-id] (connect frag node (node-mapping n-id)))
            fragment1 
            root-neighbours)))

(defn sprout-at-node [fragment1 fragment2 node]
  "Graft fragment2 onto fragment1 at node n-id. The root node(s) of 
  fragment2 are bonded to n-id."
  (let [min-node-id (inc (max-node fragment1))
        min-bond-id (inc (max-bond fragment1))
        node-mapping (partial + min-node-id)
        bond-mapping (partial + min-bond-id)
        root-id (get-in fragment2 [:root :nodes])
        root-pos (get-in fragment2 [:nodes root-id :pos])
        graft-pos (get-in fragment1 [:nodes node :pos])
        graft-dir (sprout-direction fragment1 node)
        xform (xform-from-to root-pos graft-dir)
        translation (translator-from-to [0 0] graft-pos)
        fragment2 (-> fragment2
                   (dissoc :root)
                   (transform-nodes (comp translation xform))
                   (remap node-mapping bond-mapping))]
      (-> fragment1
        (merge-fragments fragment2)
        (connect node (node-mapping root-id)))))
