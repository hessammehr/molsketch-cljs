(ns molsketch-cljs.functional
  (:require [molsketch-cljs.util
              :refer [max-node max-molecule displacement
                      normalize distance]]
            [molsketch-cljs.constants :refer [bond-length]]))

; returns [n (distance (:pos n) point)] where n is the closest
; node to point.
(defn nearest-node [state point]
  (let [node (->> state
              :nodes
              (apply min-key #(distance point (:pos (second %)))))]
    (update node 1 #(distance point (:pos %)))))


(defn new-node [state node-props]
  (let [id (inc (max-node state))]
    (assoc node-props :id id)))

(defn add-node [state node]
  (assoc-in state [:nodes (:id node)] node))

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
      (add-node n)
      (add-molecule m))))


(defn get-bonds [state node-id]
  (->> (:bonds state)
       vals
       (filter #(contains? (:nodes %) node-id))))

(defn connected [state node-id]
  (let [b (get-bonds state node-id)]
    (->> b
      (map #(disj (:nodes %) node-id))
      (map first))))

(defn node-displacement [state n1-id n2-id]
  (let [nodes (:nodes state)
        n1 (nodes n1-id)
        n2 (nodes n2-id)]
      (displacement (:pos n1) (:pos n2))))

(defn find-molecule [state node-id])

(defn new-bond [state bond-props])

(defn add-bond [state b])

(defn sprout-bond [state node-id]
  (let [cur-pos (get-in state [:nodes node-id :pos])
        vecs (map (partial node-displacement state node-id)
              (connected state node-id))
        sum (apply map + vecs)
        new-dir (map - sum)
        new-pos (mapv + cur-pos (normalize new-dir bond-length))
        n (new-node state {:pos new-pos})]
      (-> state
        (add-node n)
        (update-in [:molecules 0 :nodes] conj (:id n)))))
