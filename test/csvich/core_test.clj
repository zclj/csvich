(ns csvich.core-test
  (:use clojure.test
        csvich.core
        csvich.utils))

(deftest build-headers-from-strings
  (testing "Given strings representing headers, when headers are build, then headers are returned as a map with identity as transform"
    (let [headers (build-headers ["Name" "Age" "Weight"])]
      (is (= (map :name headers) '("Name" "Age" "Weight")))
      (is (= (map :info headers) '(#{} #{} #{})))
      (is (= (apply-headers headers [[1 2 3]])
             [{:weight 3 :name 1 :age 2}])))))

(deftest headers-can-transform-when-info-contains
  (testing "Given headers that contain info, when applied to data, then transforms are applied"
    (let [headers (build-headers
                   ["Name" "Age" "Weight (kg)"]
                   [:when-header :info :contains "kg" :do-apply [(partial * 1000)]])]
      (is (= (apply-headers headers [["Bobo" 50 67]])
             [{:name "Bobo" :age 50 :weight 67000}])))))

(deftest transforms-are-applied-in-left-to-right-order
  (testing "Given multiple transforms, when applied to data, then transforms are applied from left to right"
    (let [headers (build-headers
                   ["Name" "Age" "Weight (kg)"]
                   [:when-header :info :contains "kg" :do-apply [(partial - 10) (partial + 5)]])]
      (is (= (apply-headers headers [["Bobo" 50 20]])
             [{:name "Bobo" :age 50 :weight -5}])))))

(deftest to-numbers
  (testing "Number transformation"
    (let [ints '("1" "-2" "+3" "A4" "5B" "a6b")
          floats '("1,1" "-1,2" "+1,3" "A1,4" "1,5B" "a1,6b")
          floats2 '("1.1" "-1.2" "+1.3" "A1.4" "1.5B" "a1.6b")]
      (is (= (map to-number ints) '(1 -2 3 "A4" "5B" "a6b")))
      (is (= (map to-number floats) '(1.1 -1.2 1.3 "A1,4" "1,5B" "a1,6b")))
      (is (= (map to-number floats2) '(1.1 -1.2 1.3 "A1.4" "1.5B" "a1.6b"))))))

(deftest headers-can-transform-when-info-equals
  (testing "Given headers that equals info, when applied to data, then transforms are applied"
    (let [headers (build-headers
                   ["Name" "Age (1)" "Weight (1)(2)"]
                   [:when-header :info :equals "1" :do-apply [(partial * 1000)]])]
      (is (= (apply-headers headers [["Bobo" 50 67]])
             [{:name "Bobo" :age 50000 :weight 67}])))))

(deftest transformation-validation
  (testing "Given bad input, when validating transform, then result and message is returned"
    (let [transforms
          [[:when-header :info :contains "kg" :do-apply [(partial + 2)]]
           [:when-header :info :equals "kg" :do-apply [(partial + 2)]]
           [:whn-header :info :contains "kg" :do-apply [(partial + 2)]]
           [:when-header :ifo :contains "kg" :do-apply [(partial + 2)]]
           [:when-header :info :conains "kg" :do-apply [(partial + 2)]]
           [:when-header :info :contains "kg" :do-aply [(partial + 2)]]]
          validations (map validate-transform transforms)]
      (is (= (map first validations) [true true false false false false]))
      (is (= (map string? validations [true true true true true true]))))))
