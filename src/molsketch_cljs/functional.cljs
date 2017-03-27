;; This namespaces enables querying and functional transformation of the state
;; functions in this namespaces always take state as their first argument and
;; return a new state in case of a transformation.

(ns molsketch-cljs.functional
  (:require
    [molsketch-cljs.templates :refer [templates]]
    [molsketch-cljs.fragment.query 
      :refer [max-node max-bond sprout-direction]]
    [molsketch-cljs.fragment.xformations 
      :refer [delete-bond delete-node transform remap merge-fragments connect]]
    [molsketch-cljs.util
      :refer [xform-from-to translator-from-to]]
    [com.rpl.specter :refer [ALL FIRST LAST MAP-VALS VAL]]))

(defn active [status]
  (or (get status :hovered)
      (get status :selected)))

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
        translation (translator-from-to [0 0] graft-pos)
        fragment (-> fragment2
                  (dissoc :root)
                  (transform (comp translation xform))
                  (remap node-mapping bond-mapping))]
      (-> fragment1
        (merge-fragments fragment)
        (connect node (node-mapping root-id)))))

