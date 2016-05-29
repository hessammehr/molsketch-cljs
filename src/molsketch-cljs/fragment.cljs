(ns molsketch-cljs.fragment
  #_(:require-macros [com.rpl.specter :refer [transform]])
  (:require [molsketch-cljs.util :as u]
            [com.rpl.specter
             :refer [ALL FIRST LAST transform]
             :include-macros true]))

(defn remap [fragment node-mapping bond-mapping]
  (->> fragment
    (transform [:nodes ALL FIRST] node-mapping)
    (transform [:bonds ALL LAST :nodes ALL] node-mapping)
    (transform [:bonds ALL FIRST] bond-mapping)))

(defn translate [fragment v]
  (transform [:nodes ALL LAST :pos] (partial mapv + v) fragment))

(defn rotate [fragment degrees [cx cy]]
  (transform [:nodes ALL LAST :pos]
   #(u/rotate % degrees [cx cy]) fragment))
