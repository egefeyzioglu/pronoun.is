(ns pronouns.resource-test
  (:require [pronouns.util :as util]
            [clojure.test :refer [deftest is]]
            [clojure.java.io :as io]))

(deftest ^:unit valid-pronouns-table
  (let [table (util/slurp-tabfile (io/resource "pronouns.tab"))]
    (is table "pronouns.tab exists and is non-empty")
    (doseq [row table]
      (is (= (count row) 5)
          "row has five elements")
      (is (re-matches #".*sel(f|ves)$" (last row))
          "final element is reflexive"))))
