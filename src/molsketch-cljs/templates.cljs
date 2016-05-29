(ns molsketch-cljs.templates
  (:require [molsketch-cljs.constants :as const]))

;; l^2 = 2r^2 (1-cos(theta))
(defn ring [n]
  (let [theta (/ (* 2 (.-PI js/Math)) n)
        r (/ const/bond-length (Math.sqrt (* 2 (- 1 (Math/cos theta)))))
        nodes (into {} (for [a (range n) :let [t (* a theta)]]
                         [a {:pos [(- (* r (Math/cos t))) (* r (Math/sin t))]}]))
        bonds (into {(dec n) {:nodes #{0 (dec n)}}} ; closing bond
                    (for [b (range (dec n))]
                         [b {:nodes #{b (inc b)}}]))]
    {:nodes nodes :bonds bonds :root [:nodes 0]}))

(def templates
  {:methyl {:nodes {0 {:pos [0 0]}}
            :bonds nil
            :root [:nodes 0]}
   :cyclopropyl (ring 3)
   :cyclobutyl (ring 4)
   :cyclopentyl (ring 5)
   :cyclohexyl (ring 6)})
