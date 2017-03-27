;; This namespaces enables querying and functional transformation of the state
;; functions in this namespaces always take state as their first argument and
;; return a new state in case of a transformation.

(ns molsketch-cljs.functional
  (:require
    [molsketch-cljs.constants :refer [bond-length fuse-tolerance]]
    [molsketch-cljs.templates :refer [templates]]
    [molsketch-cljs.fragment.query 
      :refer [max-node max-bond nodes-within node-displacement connected
              node-inside nearest-node get-bonds]]
    [molsketch-cljs.fragment.xformations 
      :refer [delete-bond delete-node transform remap merge-fragments]]
    [molsketch-cljs.util
      :refer [normalize distance rotate-degrees invert angle xform-from-to
              translator-from-to orient-along]]
    [clojure.set :refer [difference]]
    [com.rpl.specter :refer [ALL FIRST LAST MAP-VALS VAL]]))

(defn max-molecule [state]
  (apply max (keys (:molecules state))))

(defn new-node [fragment node-props]
  (let [id (inc (max-node fragment))]
    [id node-props]))

(defn add-node [fragment [n-id node-props]]
  (-> fragment
      (assoc-in [:nodes n-id] node-props)))

(defn find-molecule [state node-id]
  (->> (:molecules state)
       (keep (fn [[m-id {nodes :nodes}]]
               (when (nodes node-id) m-id)))
       first))

(defn new-bond [fragment bond-props]
  "Returns a new hashmap representing a bond produced by associng an
  auto-incremented :id to the bond-propes provided."
  [(inc (max-bond fragment)) bond-props])

(defn add-bond [fragment [b-id b-props]]
  "Appends a bond to given fragment."
  (-> fragment
      (assoc-in [:bonds b-id] b-props)))
      ;(update-in [:molecules mol-id :bonds] conj (:id b))))

(defn fusion-candidates [fragment node-id]
  (let [pos (get-in fragment [:nodes node-id :pos])
        nearby (nodes-within fragment pos bond-length fuse-tolerance)
        bonded (connected fragment node-id)]
    (difference nearby bonded)))

; (defn join-molecules [state m1-id m2-id]
;   "Merges molecules m1-id and m2-id, removing m2-id in the process. Does nothing
;   if m1-id and m2-id are the same."
;   (if (= m1-id m2-id) state
;       (let [m2 (get-in state [:molecules m1-id])]
;         (-> state
;             (update-in [:molecules m1-id :bonds]
;                        into (:bonds m2))
;             (update-in [:molecules m1-id :nodes]
;                        into (:nodes m2))
;             (dissoc m2-id)))))

(defn connect [fragment n1-id n2-id]
  (let [b (new-bond fragment {:nodes #{n1-id n2-id}})]
    (-> fragment
        (add-bond b))))

(defn sprout-direction [fragment node-id & {order :order :or {order 1}}]
  (let [vecs (map (partial node-displacement fragment node-id)
                  (connected fragment node-id))
        sum (invert (apply map + vecs))
        new-dir (case (count vecs)
                  0 (rotate-degrees [1 0] -30)
                  1 (if (> order 1) sum (rotate-degrees sum -30))
                  sum)]
      (normalize new-dir bond-length)))

(defn sprout-position [fragment node-id]
  (let [cur-pos (get-in fragment [:nodes node-id :pos])
        new-dir (sprout-direction fragment node-id)]
    (mapv + cur-pos new-dir)))

(defn sprout-bond [fragment node-id]
  (if-let [c (first (fusion-candidates fragment node-id))]
    (connect fragment node-id c)
    (let [new-pos (sprout-position fragment node-id)
          n (new-node fragment {:pos new-pos})
          b (new-bond fragment {:nodes #{node-id (first n)}})]
      (-> fragment
          (add-node n)
          (add-bond b)))))

(defn active [status]
  (or (get status :hovered)
      (get status :selected)))

(defn delete [state [type id]]
  (let [s (assoc-in state [:status :hovered] nil)]
       (case type
             :nodes (delete-node s id)
             :bonds (delete-bond s id))))

(defn graft-at-node [state fragment node]
  (let [min-node-id (inc (max-node state))
        min-bond-id (inc (max-bond state))
        node-mapping (partial + min-node-id)
        bond-mapping (partial + min-bond-id)
        root-id (get-in fragment [:root :nodes])
        root-pos (get-in fragment [:nodes root-id :pos])
        graft-pos (get-in state [:nodes node :pos])
        graft-dir (sprout-direction state node)
        xform (xform-from-to root-pos graft-dir)
        translation (translator-from-to [0 0] graft-pos)
        fragment (-> fragment
                  (dissoc :root)
                  ;(update :nodes dissoc root-id)
                  (transform (comp translation xform))
                  (remap node-mapping bond-mapping))]
                  ; (transform translation))] 
      (-> state
        (merge-fragments fragment)
        (connect node (node-mapping root-id)))))
      
        

    

; (defn graft [state template at]
;   (let [min-node-id (inc (max-node state))
;         min-bond-id (inc (max-bond state))
;         root (:root template)
;         root-pos (get-in template [:root :pos])
;         graft-pos (get-in state [:nodes at :pos])
;         graft-dir (sprout-direction state at)
;         node-mapping (into {} (for [id template-node-ids] [id (+ min-node-id id)]))
;         node-mapping (assoc node-mapping root at)
;         bond-mapping (into {} (for [id template-bond-ids] [id (+ min-bond-id id)]))
;         translation (mapv + (invert root-pos) graft-pos)
;         template (-> template
;                      (dissoc :roots)
;                      (dissoc-in (:root template))
;                      (frag/rotate (angle graft-dir) root-pos)
;                      (frag/translate translation)
;                      (frag/remap node-mapping bond-mapping))]
;     (println graft-dir (angle graft-dir))
;     (merge-with merge state template)))
