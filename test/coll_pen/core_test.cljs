(ns coll-pen.core-test
    (:require
     [cljs.test :refer-macros [deftest is testing]]
     #_[coll-pen.core :refer [multiply]]))

#_(deftest multiply-test
  (is (= (* 1 2) (multiply 1 2))))

#_(deftest multiply-test-2
  (is (= (* 75 10) (multiply 10 75))))
