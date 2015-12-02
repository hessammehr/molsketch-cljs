(ns molsketch-cljs.functional
  (:require [molsketch-cljs.util :refer [max-node]]))

(defn add-molecule [m]
  (let [{nodes :nodes }]))

(defn add-free-node [state node]
   (let [{id :id :or {id (inc (max-node (:nodes state)))}} node]
    (-> state
      (assoc-in [:nodes id] (assoc node :id id))
      (update-in [:molecules ]))))

(defn sprout-bond [state node] state)
