;; This namespace has some overlap in functionality with
;; molsketch-cljs.functional. Functions more suited to fragment (as opposed to
;; state) querying/manipulation live in molsketch.fragment.

(ns molsketch-cljs.fragment.query
  (:require-macros [com.rpl.specter :refer
                    [select]])
  (:require
   [molsketch-cljs.util :as u]
   [com.rpl.specter
    :refer [ALL FIRST LAST VAL MAP-VALS]
    :include-macros true]
   [molsketch-cljs.util
    :refer [distance distance-bond displacement]]))

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

(defn nearest-bond [fragment point]
  "Returns the node in fragment that is closes to point."
    (->> fragment
       :bonds
       keys
       (apply min-key #(distance-bond fragment % point))))

(defn nodes-within [fragment point radius tol]
  "Returns the set of nodes in state whose distance to point is between
  radius-tol and radius+tol."
  (let [ks (keys (:nodes fragment))
        ns (vals (:nodes fragment))
        ds (map #(distance point (:pos %)) ns)
        dds (map #(.abs js.Math (- % radius)) ds)
        within (keep-indexed #(when (< %2 tol) (nth ks %1)) dds)]
    (into #{} within)))

(defn node-inside [fragment point radius]
  "Returns the closest node in state that is within radius of point."
  (let [n (nearest-node fragment point)
        p (get-in fragment [:nodes n :pos])]
    (when (< (distance p point) radius) n)))

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
