(ns mba.archive-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer [deftest is]]
            [mba.archive :as archive]))

(deftest filter-preparation
  (is (= ["!statuscode:200"] (archive/prepare-filters {:!statuscode 200})))
  (is (= ["statuscode:200"] (archive/prepare-filters {::archive/statuscode 200}))))

(deftest instrumented
  (stest/instrument `archive/prepare-filters)
  (s/exercise-fn `archive/prepare-filters)
  (stest/unstrument))

(deftest query-params-preparation
  (stest/unstrument)
  (is (= {:filter ["!statuscode:200"]
          :url    "jocas.lt"}
         (archive/prepare-query-params
           {}
           {:url    "jocas.lt"
            :filter {::archive/!statuscode 200}})))
  (is (= {:filter ["statuscode:200"]
          :url    "jocas.lt"}
         (archive/prepare-query-params
           {}
           {:url    "jocas.lt"
            :filter {::archive/statuscode 200}}))))
