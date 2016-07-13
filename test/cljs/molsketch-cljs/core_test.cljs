(ns molsketch-cljs.core-test
  (:require-macros [com.rpl.specter.macros :refer [select]])
  (:require
   [molsketch-cljs.functional :as fun]
   [molsketch-cljs.util :as util]
   [molsketch-cljs.fragment :as frag]
   [molsketch-cljs.templates :as tmplt]
   [com.rpl.specter :refer [ALL FIRST LAST]]
   [cljs.test :refer-macros [deftest testing is run-tests]]))


(deftest test-fragment []
  (let [f (tmplt/templates :cyclopropyl)]
    (is (= [0 1 2] (select [:nodes ALL FIRST] f)))
    (is (= 1 3))))

(enable-console-print!)
(run-tests)

(defn on-js-reload []
  (.clear js/console)
  (run-tests))
