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
(def defaultGoodPrice
  "The default price of goods and services per unit"
  1
  )
(def goods
  "A map of goods and services to details"
  {:power {;; The readable name of the good or service
           :title "Electricity"
           ;; The minimum clearance that is legally allowed to consume this item. If absent, is :IR
           :consumeClearance :IR
           ;; The minimum clearance that is legally allowed to produce this item. If absent, is :IR
           :productionClearance :IR
           ;; Is this item something that needs to be gotten rid of?
           :waste? false
           ;; essential? is this provided as a service by fc for infrareds?
           :essential? true
           ;; clearanceSameCost? does this cost the same for all clearances? (Quality generally differs per clearance, as does cost
           :clearanceSameCost? true
           ;; Is this good illegal? (Goods are always illegal below their allowed clearance, this just makes it easier to specify)
           }
   :air {:title "Breathable Air"
         }
   :pgc {:title "Protogummicarb"
         }
   :algaeFood {:title "Synthetic Food"
               }
   :paperwork {:title "Paperwork"
             :waste? true
             }
   }
  )
(def reactions
  "A map of reactions to details"
  {:pgc {;; What this reaction is called
         :title "PGC Production"
         ;; What goods are consumed in this reaction, per daycycle
         :consumes {:paperwork 1
                    }
         ;; What goods are produced in this reaction, per daycycle
         :produces {:pgc 40
                    }
         ;; Is this an illegal action? (For use in black market etc)
         :illegal? false
         }
   :algaeFood {:title "Synthetic Foodstuffs"
               :consumes {:pgc 1}
               :produces {:algaeFood 1}
               }
   :power {:title "Electricity Production"
           :consumes {}
           :produces {:power 10}
           }
   :paperwork {:title "Paperwork"
               :consumes {}
               :produces {:paperwork 5}
               }
   :air {:title "Air Recycling"
         :consumes {}
         :produces {:air 10}
         }
   }
  )
(def serviceGroups
  "A map of service groups and their outputs"
  {:AF {:title "Armed Forces"
        :goods [:bodyguards :wmds :defence]
        }
   }
  )
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

;; Goods and services information
(s/def ::price number?)
(s/def ::quantity number?)
(s/def ::goodType (s/and keyword? #((set (keys goods)) %)))
(s/def ::good (s/keys :req-un [::price ::quantity ::goodType]))
;; goods is of the form {:pgc {:IR {:price 1 :quantity 1}}}
(s/def ::goods (s/map-of ::goodType (s/map-of ::clearance ::good)))

;; Validate what's called is one of the reactions
(s/def ::reactionType (s/and keyword? #((set (keys reactions)) %)))

;; Player information
(s/def ::userKey (s/and string?
                        #(pos? (count %))))
(s/def ::player (s/keys :req-un [::userKey ::citizenId]))
(s/def ::players (s/map-of ::userKey ::player))
;; Players may not yet have information on associates
(s/def ::citizenMap (s/keys :req-un [::citizenId ::clearance ::fName ::zone ::cloneNum ::gender ::commendations ::treason]
                            :opt-un [::associates ::dead?]))
(s/def ::citizens (s/map-of ::citizenId ::citizenMap))
(s/def ::sector (s/keys :req-un [::zone ::citizens ::goods]))
