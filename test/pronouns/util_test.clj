(ns pronouns.util-test
  (:require [pronouns.util :as util]
            [clojure.test :refer [deftest testing are]]))

(def test-table [["ze" "hir" "hir" "hirs" "hirself"]
                 ["ze" "zir" "zir" "zirs" "zirself"]
                 ["she" "her" "her" "hers" "herself"]
                 ["he" "him" "his" "his" "himself"]
                 ["they" "them" "their" "theirs" "themselves"]
                 ["they" "them" "their" "theirs" "themself"]])

(deftest ^:unit table-filters
  (testing "table-front-filter"
    (are [arg return] (= (util/table-front-filter arg test-table) return)
      ["she"] [["she" "her" "her" "hers" "herself"]]
      ["ze"] [["ze" "hir" "hir" "hirs" "hirself"]
              ["ze" "zir" "zir" "zirs" "zirself"]]
      ["ze" "zir"] [["ze" "zir" "zir" "zirs" "zirself"]]))

  (testing "table-end-filter"
    (are [arg return] (= (util/table-end-filter arg test-table) return)
      ["themself"] [["they" "them" "their" "theirs" "themself"]]
      ["themselves"] [["they" "them" "their" "theirs" "themselves"]])))

(deftest ^:unit table-lookup
  (are [arg return] (= (util/table-lookup arg test-table) return)
    ["she"] ["she" "her" "her" "hers" "herself"]
    ["ze"] ["ze" "hir" "hir" "hirs" "hirself"]
    ["ze" "zir"] ["ze" "zir" "zir" "zirs" "zirself"]
    ["they"] ["they" "them" "their" "theirs" "themselves"]
    ["they" "..." "themself"] ["they" "them" "their" "theirs" "themself"]))

(deftest ^:unit shortest-path
  (testing "shortest forward path"
    (are [row return]
         (= (util/shortest-unambiguous-forward-path test-table row) return)
      ["ze" "hir" "hir" "hirs" "hirself"] ["ze" "hir"]
      ["ze" "zir" "zir" "zirs" "zirself"] ["ze" "zir"]
      ["they" "them" "their" "theirs" "themselves"] ["they" "them" "their" "theirs" "themselves"]
      ["she" "her" "her" "hers" "herself"] ["she"]))

  (testing "shortest ellipses path"
    (are [row return]
         (= (util/shortest-unambiguous-ellipses-path test-table row) return)
      ["ze" "hir" "hir" "hirs" "hirself"] ["ze" "..." "hirself"]
      ["they" "them" "their" "theirs" "themselves"] ["they" "..." "themselves"]
      ["she" "her" "her" "hers" "herself"] ["she" "..." "herself"]))

  (testing "shortest overall path"
    (are [row return]
         (= (util/shortest-unambiguous-path test-table row) return)
      ["ze" "hir" "hir" "hirs" "hirself"] "ze/hir"
      ["they" "them" "their" "theirs" "themselves"] "they/.../themselves"
      ["she" "her" "her" "hers" "herself"] "she")))

