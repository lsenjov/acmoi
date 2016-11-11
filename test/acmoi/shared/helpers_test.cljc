(ns acmoi.shared.helpers-test
  (:require [#?(:clj clojure.spec
               :cljs cljs.spec) :as s]
            [clojure.test :refer :all]
            [acmoi.shared.spec :as ss]
            [acmoi.shared.helpers :as ach]
            )
  )

(deftest parse-int-test
  (testing "Correct use cases"
    (and
      (= 5 (ach/parse-int "5"))
      (= 0 (ach/parse-int "0"))
      (= -5 (ach/parse-int "-5"))
      )
    )
  (testing "Returning nil"
    (and
      (nil? (ach/parse-int "asdf"))
      (nil? (ach/parse-int "0.5"))
      (nil? (ach/parse-int "-8."))
      )
    )
  )

(def testMap {:a 1 :b 2 :c 3 :d 4 :e 5})
(deftest filter-keys-test
  (testing "No keys"
    (let [r (ach/filter-keys testMap [])]
      (and (map? r)
           (= 0 (count r))
           )
      )
    )
  (testing "No results"
    (let [r (ach/filter-keys testMap [:f :g])]
      (and (map? r)
           (= 0 (count r))
           )
      )
    )
  (testing "Correct use case"
    (let [r (ach/filter-keys testMap [:a :b])]
      (= {:a 1 :b 2} r)
      )
    )
  )
