;; This namespaces enables querying and functional transformation of the state
;; functions in this namespaces always take state as their first argument and
;; return a new state in case of a transformation.

(ns molsketch-cljs.functional
  (:require-macros [com.rpl.specter.macros
                    :refer [select transform]])
  (:require
   [molsketch-cljs.constants :refer [bond-length fuse-tolerance]]
   [molsketch-cljs.fragment :refer [connected remap]]
   [molsketch-cljs.templates :refer [templates]]
   [molsketch-cljs.util
    :refer [max-node max-bond max-molecule displacement
            normalize distance rotate-degrees invert
            angle xform-from-to
            translator-from-to]]
   [clojure.set :refer [difference]]
   [com.rpl.specter :refer [ALL FIRST LAST MAP-VALS VAL]]))

(declare nodes-within delete-bond)

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
  (assoc bond-props :id (inc (max-bond state))))

(defn add-bond [state b mol-id]
  "Appends the hashmap b describing a bond to state and and molecule mol-id."
  (-> state
      (assoc-in [:bonds (:id b)] b)
      (update-in [:molecules mol-id :bonds] conj (:id b))))

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
  (let [b (new-bond state {:nodes #{n1-id n2-id}})
        m1 (find-molecule state n1-id)
        m2 (find-molecule state n2-id)]
    (-> state
        (join-molecules m1 m2)
        (add-bond b m1))))

(defn sprout-direction [state node-id & {order :order :or {order 1}}]
  (let [vecs (map (partial node-displacement state node-id)
                  (connected state node-id))
        sum (apply map + vecs)
        new-dir (invert sum)]
    (case (count vecs)
      0 (rotate-degrees [1 0] -30)
      1 (if (> order 1) new-dir
            (rotate-degrees new-dir -30))
      new-dir)))

(defn sprout-position [state node-id]
  (let [cur-pos (get-in state [:nodes node-id :pos])
        new-dir (sprout-direction state node-id)]
    (mapv + cur-pos (normalize new-dir bond-length))))

(defn sprout-bond [state node-id]
  (if-let [c (first (fusion-candidates state node-id))]
    (connect state node-id c)
    (let [new-pos (sprout-position state node-id)
          n (new-node state {:pos new-pos})
          b (new-bond state {:nodes #{node-id (:id n)}})
          m (find-molecule state node-id)]
      (-> state
          (add-node n m)
          (add-bond b m)))))

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
        root-pos (get-in fragment [:nodes root-id])]
    (-> template
        (remap node-mapping bond-mapping))
    ))

(defn graft [state template at]
  (let [min-node-id (inc (max-node state))
        min-bond-id (inc (max-bond state))
        roots (:roots template)
        root-pos (get-in template [typ root :pos])
        graft-pos (get-in state [:nodes at :pos])
        graft-dir (sprout-direction state at)
        node-mapping (into {} (for [id template-node-ids] [id (+ min-node-id id)]))
        node-mapping (assoc node-mapping root at)
        bond-mapping (into {} (for [id template-bond-ids] [id (+ min-bond-id id)]))
        translation (mapv + (invert root-pos) graft-pos)
        template (-> template
                     (dissoc :roots)
                     (dissoc-in (:root template))
                     (frag/rotate (angle graft-dir) root-pos)
                     (frag/translate translation)
                     (frag/remap node-mapping bond-mapping))]
    (println graft-dir (angle graft-dir))
    (merge-with merge state template)))

;; (defmulti graft (fn [state template at] (first at)))

;; (defmethod graft :bonds [state template at])
;; (defmethod graft :nodes [state template [_ node-id]]
;;   (let [node-shift #(+ % (inc (max-node state)))
;;         bond-shift #(+ % (inc (max-bond state)))
;;         sprout-dir (sprout-direction state node-id)
;;         graft-dir (:graft-dir template)
;;         graft-pos (sprout-position state node-id)
;;         root-pos (get-in template [:nodes node-id :pos])
;;         translation1 (translator-from-to root-pos [0 0])
;;         translation2 (translator-from-to [0 0] graft-pos)
;;         rotation (xform-from-to root-pos graft-dir)]
;;     (->> template
;;       (transform [:nodes ALL FIRST] node-shift)
;;       (transform [:bonds MAP-VALS :nodes ALL] node-shift)
;;       (transform [:bonds ALL FIRST] bond-shift)
;;       (transform [:nodes MAP-VALS :pos] translation1)
;;       (transform [:nodes MAP-VALS :pos] rotation)
;;       (transform [:nodes MAP-VALS :pos] translation2))))
