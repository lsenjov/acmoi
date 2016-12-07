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


(def econMap
  (-> {}
      (assoc ::es/goods es/goods)
      (assoc ::es/reactions es/reactions)
      )
  )

(s/explain ::es/good (get-in econMap [::es/goods :algaeFood]))
(get-good econMap :pgc)
(get-good-price econMap :U :pgc)
(h/valid? ::ss/clearance :IR)
(get-total-price econMap :IR {:algaeFood 1})
(get-reaction-profit econMap :pgc :IR)
(->> (for [clearance (keys ss/clearances)
           reaction (keys es/reactions)]
       {:r reaction :c clearance :cost (get-reaction-profit econMap reaction clearance)}
       )
     (sort-by #(ss/clearanceOrder (:c %)))
     (sort-by :r)
     (map (fn [{:keys [r c cost]}] (str "Reaction: " r ", Clearance: " c ", Cost: " cost)))
     (interpose \newline)
     (apply str)
     (spit "test.out")
     )
