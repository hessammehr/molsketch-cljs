(ns molsketch-cljs.core-test
  (:require
   [molsketch-cljs.core :as c]
   [molsketch-cljs.functional :as f]
   [molsketch-cljs.util :as u]
   [molsketch-cljs.fragment.query :as q]
   [molsketch-cljs.fragment.xformations :as x]
   [molsketch-cljs.templates :as t]
   [cljs.test :refer-macros [deftest testing is run-tests]]))


; (deftest test-fragment
;  (let [f (tmplt/templates :cyclopropyl)
;        n-mapping {0 5 1 8 2 9 3 4}
;        b-mapping {0 4 1 9 2 7 3 5}
;        fp (frag/remap f n-mapping b-mapping)
;        xform (util/rotator-from-to [1 0] [0 1])]
;    (is (= (into #{} (select [:nodes ALL FIRST] fp)) #{5 8 9}))
;    (is (= (into #{}(select [:bonds ALL FIRST] fp)) #{4 9 7}))
;    (is (= (:roots fp) #{[:nodes 5] [:bonds 4]}))
;    (is (= [-0.5 0.5] (xform [0.5 0.5])))
;    (is (= true (do (println (fun/graft blank-state f [:nodes 1]) true))))
;    (is (= 0 (frag/order (tmplt/templates :methyl))))
;    (is (= 2 (frag/order (tmplt/templates :cyclobutyl))))
;    ))

(def test-state
  {:nodes
   {0 {:pos [65 30] :elem :P}
    1 {:pos [90 50]}
    2 {:pos [90 80] :elem :O}}
   :bonds
   {0 {:nodes #{0 1}}
    1 {:nodes #{1 2}}}
   :molecules
   {0 {:nodes #{0 1 2} :bonds #{0 1}}}
   :status
   {:mode :normal :mouse nil :key-seq []}})

(reset! c/app-state test-state)

(def s c/app-state)

(deftest test-dummy
  (is (= 1 1)))

(deftest test-max-node
  (is (= 2 (q/max-node @s))))

(deftest test-max-bond
  (is (= 1 (q/max-bond @s))))

(enable-console-print!)

(defn on-js-reload []
  (.clear js/console)
  (println "Running tests...")
  (run-tests))
