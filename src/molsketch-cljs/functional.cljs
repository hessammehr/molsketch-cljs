(ns molsketch-cljs.functional
  (:require [molsketch-cljs.util :refer [max-node max-molecule]]))

; (defn add-molecule [m]
;   (let [{nodes :nodes}]))

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


(defn get-bonds [state node]
  (->> (:bonds state)
       vals
       (filter #(contains? (:nodes %) node))))

(defn sprout-bond [state node] state)
