(ns molsketch-cljs.core-test
  (:require
   [molsketch-cljs.functional :as fun]
   [molsketch-cljs.util :as util]
   [molsketch-cljs.fragment :as frag]
   [molsketch-cljs.templates :as temp]
   [cljs.test :refer-macros [deftest testing is run-tests]]))

(deftest test-test []
  (is (= 1 4)))

; (defn run-tests []
;   (.clear js/console)
;   (cljs.test/run-all-tests #"molsketch-cljs.*-test"))

(enable-console-print!)
(run-tests)

(defn on-jsload []
  (run-tests))
