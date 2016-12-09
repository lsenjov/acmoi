(ns acmoi.shared.economy-test
  (:require [#?(:clj clojure.spec
               :cljs cljs.spec) :as s]
            [clojure.test :refer :all]
            [acmoi.shared.spec :as ss]
            [acmoi.shared.helpers :as h]
            [acmoi.shared.economy :refer :all :as es]
            )
  )

(s/check-asserts false)
(s/check-asserts true)


;; The default economy map
(def econMap
  (-> {}
      (assoc ::es/goods es/goods)
      (assoc ::es/reactions es/reactions)
      )
  )

(deftest basicOperation
  (testing "Basic reactions. Shouldn't throw exceptions"
    (doall
      (for [clearance (keys ss/clearances)
            reaction (keys es/reactions)]
        (testing (str "Clearance: " clearance ", reaction:" reaction)
          (is (get-reaction-profit econMap reaction clearance) "Should not be nil!")
          )
        )
      )
    )
  (testing "Basic goods commands. Shouldn't throw exceptions"
    (doall
      (for [clearance (keys ss/clearances)
            good (keys es/goods)]
        (do
          (is (get-good-price econMap clearance good) "Should not be nil!")
          (is (get-good econMap good) "Should not be nil!")
          )
        )
      )
    )
  (testing "Bad commands"
    ;; Invalid items should return nil
    (is (nil? (get-reaction-profit econMap :invalid :IR)))
    (is (nil? (get-good-price econMap :IR :invalid)))
    (is (nil? (get-good econMap :invalid)))
    ;; Invalid clearances should throw exceptions
    (is (thrown? java.lang.AssertionError (get-reaction-profit econMap :invalid :None)))
    (is (thrown? java.lang.AssertionError (get-good-price econMap :None :invalid)))
    )
  )
