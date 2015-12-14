(ns molsketch-cljs.functional
  (:require [molsketch-cljs.util
             :refer [max-node max-bond max-molecule displacement
                     normalize distance rotate]]
            [molsketch-cljs.constants :refer [bond-length
                                              fuse-tolerance]]
            [clojure.set :refer [difference]]))

(defn add-class [node class]
  (update node :class str " " class))

(defn prepare-selected [state]
  (if-let [x (get-in state [:status :selected])]
    (update-in state x add-class "selected")
    state))

(defn prepare-hovered [state]
  (if-let [x (get-in state [:status :hovered])]
    (update-in state x add-class "hovered")
    state))

(def prepare (comp prepare-hovered prepare-selected))

(defn nearest-node [state point]
  (->> state
    :nodes
    (apply min-key #(distance point (:pos (second %))))
    first))

(defn node-inside [state point radius]
  (let [n (nearest-node state point)
        pos (get-in state [:nodes n :pos])
        d (distance pos point)]
    (when (< d radius) n)))

(defn nodes-within [state point radius tol]
  (let [ks (keys (:nodes state))
        ns (vals (:nodes state))
        ds (map #(distance point (:pos %)) ns)
        dds (map #(.abs js.Math (- % radius)) ds)
        within (keep-indexed #(when (< %2 tol) (nth ks %1)) dds)]
    (into #{} within)))

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


(defn get-bonds [state node-id]
  (->> (:bonds state)
    (keep (fn [[b-id {nodes :nodes}]]
            (when (nodes node-id) b-id)))
    (into #{})))

(defn connected [state node-id]
  (->> (get-bonds state node-id)
    (map #(get-in state [:bonds % :nodes]))
    (map #(disj % node-id))
    (map first)
    (into #{})))

(defn node-displacement [state n1-id n2-id]
  (let [nodes (:nodes state)
        n1 (nodes n1-id)
        n2 (nodes n2-id)]
    (displacement (:pos n1) (:pos n2))))

(defn find-molecule [state node-id]
  (->> (:molecules state)
    (keep (fn [[m-id {nodes :nodes}]]
            (when (nodes node-id) m-id)))
    first))

(defn new-bond [state bond-props]
  (assoc bond-props :id (inc (max-bond state))))

(defn add-bond [state b mol-id]
  (-> state
    (assoc-in [:bonds (:id b)] b)
    (update-in [:molecules mol-id :bonds] conj (:id b))))

(defn fusion-candidates [state node-id]
  (let [pos (get-in state [:nodes node-id :pos])
        nearby (nodes-within state pos bond-length fuse-tolerance)
        bonded (connected state node-id)]
    (difference nearby bonded)))

(defn join-molecules [state m1-id m2-id]
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

(defn sprout-position [state node-id]
  (let [cur-pos (get-in state [:nodes node-id :pos])
        vecs (map (partial node-displacement state node-id)
               (connected state node-id))
        sum (apply map + vecs)
        new-dir (map - sum)
        new-dir (case (count vecs)
                  0 (rotate [1 0] -30)
                  1 (rotate new-dir 60)
                  new-dir)]
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
