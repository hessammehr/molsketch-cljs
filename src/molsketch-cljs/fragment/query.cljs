;; This namespace has some overlap in functionality with
;; molsketch-cljs.functional. Functions more suited to fragment (as opposed to
;; state) querying/manipulation live in molsketch.fragment.

(ns molsketch-cljs.fragment
  (:require-macros [com.rpl.specter.macros :refer
                    [transform setval select]])
  (:require
   [molsketch-cljs.util :as u]
   [com.rpl.specter
    :refer [ALL FIRST LAST VAL MAP-VALS]
    :include-macros true]))

(defn max-node [fragment]
  (apply max (keys (:nodes fragment))))

(defn max-bond [fragment]
  (apply max (keys (:bonds fragment))))

(defn nearest-node [fragment point]
  "Returns the node in fragment that is closes to point."
  (->> fragment
       :nodes
       (apply min-key #(distance point (:pos (second %))))
       first))

(defn node-inside [fragment point radius]
  "Return a node in state that is within radius of point."
  (first (nodes-within fragment point 0 radius)))

(defn nodes-within [fragment point radius tol]
  "Returns the set of nodes in state whose distance to point is between
  radius-tol and radius+tol."
  (let [ks (keys (:nodes fragment))
        ns (vals (:nodes fragment))
        ds (map #(distance point (:pos %)) ns)
        dds (map #(.abs js.Math (- % radius)) ds)
        within (keep-indexed #(when (< %2 tol) (nth ks %1)) dds)]
    (into #{} within)))

(defn node-displacement [fragment n1-id n2-id]
  (let [nodes (:nodes fragment)
        n1 (nodes n1-id)
        n2 (nodes n2-id)]
    (displacement (:pos n1) (:pos n2))))

(defn get-bonds [fragment node-id]
  (->> (:bonds fragment)
       (keep (fn [[b-id {nodes :nodes}]]
               (when (nodes node-id) b-id)))
       (into #{})))

(defn connected [fragment node-id]
  (->> (get-bonds fragment node-id)
       (map #(get-in fragment [:bonds % :nodes]))
       (map #(disj % node-id))
       (map first)
       (into #{})))

(defn order [fragment]
  "Returns the order of fragment: the number of bonds going to its root node."
  (-> fragment
      (connected (get-in fragment [:root :nodes]))
      count))
