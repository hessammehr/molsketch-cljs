(ns molsketch-cljs.fragment
  #_(:require-macros [com.rpl.specter :refer [transform]])
  (:require [molsketch-cljs.util :refer [map-in]]
            [com.rpl.specter
             :refer [ALL FIRST LAST transform]
             :include-macros true]))

(defn remap-nodes [fragment node-mapping]
  (-> fragment
    (map-in [:nodes] (fn [[n-id n]] [(node-mapping n-id) n]))
    (map-in [:bonds] (fn [[b-id b]] [b-id (map-in b [:nodes] node-mapping)]))))


#_(defn remap-bonds [fragment bond-mapping]
  (map-in fragment [:bonds] (fn [[b-id b]] [(bond-mapping b-id) b])))

(defn remap-bonds [fragment bond-mapping]
  (transform [:bonds ALL FIRST] bond-mapping fragment))

(defn translate [fragment v]
  (println fragment v)
  (transform [:nodes ALL LAST :pos] (partial mapv + v) fragment))

(defn rotate [fragment angle [cx cy]]
  )
(defn invert [v]
  (mapv - v))
