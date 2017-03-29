;; This namespaces enables querying and functional transformation of the state
;; functions in this namespaces always take state as their first argument and
;; return a new state in case of a transformation.

(ns molsketch-cljs.functional)

(defn active [status]
  (or (get status :hovered)
      (get status :selected)))
