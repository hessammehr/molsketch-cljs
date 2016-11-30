;; This namespaces enables querying and functional transformation of the state
;; functions in this namespaces always take state as their first argument and
;; return a new state in case of a transformation.

(ns molsketch-cljs.functional
  ; (:require-macros [com.rpl.specter
  ;                   :refer [select transform]])
  (:require
   [molsketch-cljs.constants :refer [bond-length fuse-tolerance]]
   [molsketch-cljs.templates :refer [templates]]
   [molsketch-cljs.fragment.query 
    :refer [max-node max-bond nodes-within node-displacement connected
            node-inside nearest-node get-bonds]]
   (molsketch-cljs.fragment.xformations :refer [transform remap merge-fragments])
   [molsketch-cljs.util
    :refer [normalize distance rotate-degrees invert angle xform-from-to
            translator-from-to orient-along]]
   [clojure.set :refer [difference]]
   [com.rpl.specter :refer [ALL FIRST LAST MAP-VALS VAL]]))

(declare delete-bond)

(defn max-molecule [state]
  (apply max (keys (:molecules state))))

(defn new-node [state node-props]
  (let [id (inc (max-node state))]
    (assoc node-props :id id)))

(defn add-node [state node mol-id]
  (-> state
      (assoc-in [:nodes (:id node)] node)
      (update-in [:molecules mol-id :nodes] conj (:id node))))

(defn new-molecule [state mol-props]
  (let [id (inc (max-molecule state))
        {nodes :nodes bonds :bonds
         :or {nodes #{} bonds #{}}} mol-props]
    {:id id :nodes nodes :bonds bonds}))

(defn add-molecule [state mol]
  (assoc-in state [:molecules (:id mol)] mol))

(defn add-free-node [state node-props]
  (let [n (new-node state node-props)
        m (new-molecule state {:nodes #{(:id n)}})]
    (-> state
        (add-molecule m)
        (add-node n (:id m)))))

(defn find-molecule [state node-id]
  (->> (:molecules state)
       (keep (fn [[m-id {nodes :nodes}]]
               (when (nodes node-id) m-id)))
       first))

(defn new-bond [state bond-props]
  "Returns a new hashmap representing a bond produced by associng an
  auto-incremented :id to the bond-propes provided."
  [(inc (max-bond state)) bond-props])

(defn add-bond [state [b-id b-props]]
  "Appends the hashmap b describing a bond to state and and molecule mol-id."
  (-> state
      (assoc-in [:bonds b-id] b-props)))
      ;(update-in [:molecules mol-id :bonds] conj (:id b))))

(defn fusion-candidates [state node-id]
  (let [pos (get-in state [:nodes node-id :pos])
        nearby (nodes-within state pos bond-length fuse-tolerance)
        bonded (connected state node-id)]
    (difference nearby bonded)))

(defn join-molecules [state m1-id m2-id]
  "Merges molecules m1-id and m2-id, removing m2-id in the process. Does nothing
  if m1-id and m2-id are the same."
  (if (= m1-id m2-id) state
      (let [m2 (get-in state [:molecules m1-id])]
        (-> state
            (update-in [:molecules m1-id :bonds]
                       into (:bonds m2))
            (update-in [:molecules m1-id :nodes]
                       into (:nodes m2))
            (dissoc m2-id)))))

(defn connect [state n1-id n2-id]
  (let [b (new-bond state {:nodes #{n1-id n2-id}})]
        ;m1 (find-molecule state n1-id)
        ;m2 (find-molecule state n2-id)]
    (-> state
        ;(join-molecules m1 m2)
        (add-bond b))))

(defn sprout-direction [state node-id & {order :order :or {order 1}}]
  (let [vecs (map (partial node-displacement state node-id)
                  (connected state node-id))
        sum (invert (apply map + vecs))
        new-dir (case (count vecs)
                  0 (rotate-degrees [1 0] -30)
                  1 (if (> order 1) sum (rotate-degrees sum -30))
                  sum)]
      (normalize new-dir bond-length)))

(defn sprout-position [state node-id]
  (let [cur-pos (get-in state [:nodes node-id :pos])
        new-dir (sprout-direction state node-id)]
    (mapv + cur-pos new-dir)))

(defn sprout-bond [state node-id]
  (if-let [c (first (fusion-candidates state node-id))]
    (connect state node-id c)
    (let [new-pos (sprout-position state node-id)
          n (new-node state {:pos new-pos})
          b (new-bond state {:nodes #{node-id (:id n)}})
          m (find-molecule state node-id)]
      (-> state
          (add-node n m)
          (add-bond b)))))

(defn active [state]
  (get-in state [:status :hovered]
          (get-in state [:status :selected])))

(defn delete-node [state node-id]
  (let [bs (get-bonds state node-id)
        state (update state :nodes dissoc node-id)]
    (reduce delete-bond state bs)))

(defn delete-bond [state bond-id]
  (update state :bonds dissoc bond-id))

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
