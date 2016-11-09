(ns acmoi.spec
  (:require [clojure.spec :as s])
  )

(def clearances
  "A map of clearance levels and their associated standings"
  {:IR {::income 100}
   :R {::income 1000}
   }
  )
(s/def ::clearance
  (s/and string?
         #((-> clearances keys set) %)
         )
  )
