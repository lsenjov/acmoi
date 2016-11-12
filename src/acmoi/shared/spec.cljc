(ns acmoi.shared.spec
  #?(:clj (:require [clojure.spec :as s]
                    [clojure.spec.gen :as gen]
                    )
     :cljs (:require [cljs.spec :as s]))
  )

(def clearances
  "A map of clearance levels and their associated standings"
  {:IR {:income 100}
   :R {:income 1000 :oneIn 10}
   :O {:income 2000 :oneIn 16}
   :Y {:income 3000 :oneIn 30}
   :G {:income 10000 :oneIn 50}
   :B {:income 40000 :oneIn 100}
   :I {:income 100000 :oneIn 1000}
   :V {:income 600000 :oneIn 10000}
   ;; U gets 1e7 income
   :U {:income 10000000 :oneIn 1000000}
   }
  )
(def clearanceOrder
  "A map of clearances to level (0 is :IR), and level to clearances.
  Easy to convert between the two and find promotional levels"
  (let [m {:IR 0 :R 1 :O 2 :Y 3 :G 4 :B 5 :I 6 :V 7 :U 8}]
    (apply merge m (map (fn [[k v]] {v k}) m))))
(s/def ::clearance
  (s/and keyword?
         #((-> clearances keys set) %)
         )
  )
(s/def ::income (s/and integer?
                       pos?))
(s/def ::fName (s/and string?
                      #(pos? (count %))
                      )
  )
(s/def ::zone (s/and string?
                     #(= 3 (count %))))
(s/def ::cloneNum (s/and integer?
                         pos?))
(s/def ::treason (s/and integer?
                        (complement neg?)))
(s/def ::commendations ::treason)
;; Citizen IDs start at 1
(s/def ::citizenId (s/and integer?
                          pos?))
(s/def ::associates (s/coll-of ::citizenId))
(s/def ::dead? boolean?)
(s/def ::gender #(#{:male :female} %))

;; Player information
(s/def ::userKey (s/and string?
                        #(pos? (count %))))
(s/def ::player (s/keys :req-un [::userKey ::citizenId]))
(s/def ::players (s/map-of ::userKey ::player))
;; Players may not yet have information on associates
(s/def ::citizenMap (s/keys :req-un [::citizenId ::clearance ::fName ::zone ::cloneNum ::gender ::commendations ::treason]
                            :opt-un [::associates ::dead?]))
(s/def ::citizens (s/map-of ::citizenId ::citizenMap))
(s/def ::sector (s/keys :req-un [::zone ::citizens]))
