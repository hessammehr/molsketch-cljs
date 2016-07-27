(ns molsketch-cljs.core-test
  (:require-macros [com.rpl.specter.macros
                    :refer [select select-first]])
  (:require
   [molsketch-cljs.core :refer [blank-state]]
   [molsketch-cljs.functional :as fun]
   [molsketch-cljs.util :as util]
   [molsketch-cljs.fragment :as frag]
   [molsketch-cljs.templates :as tmplt]
   [com.rpl.specter :refer [ALL FIRST LAST]]
   [cljs.test :refer-macros [deftest testing is run-tests]]))


(deftest test-fragment
  (let [f (tmplt/templates :cyclopropyl)
        n-mapping {0 5 1 8 2 9 3 4}
        b-mapping {0 4 1 9 2 7 3 5}
        fp (frag/remap f n-mapping b-mapping)
        xform (util/rotator-from-to [1 0] [0 1])]
    (is (= (into #{} (select [:nodes ALL FIRST] fp)) #{5 8 9}))
    (is (= (into #{}(select [:bonds ALL FIRST] fp)) #{4 9 7}))
    (is (= (:roots fp) #{[:nodes 5] [:bonds 4]}))
    (is (= [-0.5 0.5] (xform [0.5 0.5])))
    (is (= true (do (println (fun/graft blank-state f [:nodes 1]) true))))
    (is (= 0 (frag/order (tmplt/templates :methyl))))
    (is (= 2 (frag/order (tmplt/templates :cyclobutyl))))
    ))


(enable-console-print!)
(run-tests)

(defn on-js-reload []
  (.clear js/console)
  (run-tests))
