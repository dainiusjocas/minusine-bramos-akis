(ns mba.archive-test
  (:require [clojure.test :refer [deftest is]]
            [mba.archive :as archive]))

(deftest filter-preparation
  (is (= ["statuscode:200"] (archive/prepare-filters {:statuscode 200})))
  (is (= ["!statuscode:200"] (archive/prepare-filters {:!statuscode 200}))))

(deftest query-params-preparation
  (is (= {:filter ["!statuscode:200"]
          :url    "jocas.lt"}
         (archive/prepare-query-params
           {}
           {:url    "jocas.lt"
            :filter {:!statuscode 200}}))))
