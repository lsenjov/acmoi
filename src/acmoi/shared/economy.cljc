(ns acmoi.shared.economy
  "This namespace is all about the alpha complex economy.
  It details the lists of goods, their production methods, and their clearances
  "
  (:require [clojure.spec :as s]
            [acmoi.shared.spec :as ss]
            [acmoi.shared.helpers :as h]
            )
  (:gen-class)
  )

(def defaultGoodPrice
  "The default price of goods and services per unit"
  1
  )
(def ^:private goodsDefault
  "A map of goods and services to details.
  Should be initialised to ensure ::prices and ::quantity* keywords initialised
  "
  {:power {;; The readable name of the good or service
           ::title "Electricity"
           ;; The minimum clearance that is legally allowed to consume this item. If absent, is :IR
           ::consumeClearance :IR
           ;; The minimum clearance that is legally allowed to produce this item. If absent, is :IR
           ::productionClearance :IR
           ;; Is this item something that needs to be gotten rid of?
           ;; TODO am I still using this?
           ::waste? false
           ;; essential? is this provided as a service by fc for infrareds?
           ::essential? true
           ;; clearanceSameCost? does this cost the same for all clearances? (Quality generally differs per clearance, as does cost)
           ::clearanceSameCost? true
           ;; Is this good illegal? (Goods are always illegal below their allowed clearance, this just makes it easier to specify)
           ::illegal? false
           ;; The current price of the good at each clearance level
           ;; Note that if ::clearanceSameCost? is true, only :IR will be read/modified
           ;; Also note these prices are subject to change. If the price doesn't exist here, start it at defaultGoodPrice
           ::prices {:IR 1}
           ;; This is the quantity of goods on the market
           ;; If this doesn't exist, it is zero
           ;; As above, it is by clearance
           ::quantity {:IR 0}
           ;; This is amount of goods moved last cycle (after sell phase, minus the quantity)
           ;; Used as a modifier on sale price
           ;; If 0, will act as a 1 instead
           ::quantityMoved {:IR 0}
           ;; This is the quantity on the market at the end of last cycle
           ;; Used to tell if there's a general surplus or deficit
           ::quantityLast {:IR 0}
           }
   :air {::title "Breathable Air"
         }
   :pgc {::title "Protogummicarb"
         ::prices {:IR 1 :R 3 :O 6 :Y 10 :G 30 :B 60 :I 100 :V 200 :U 1000}
         }
   :algaeFood {::title "Synthetic Food"
               ::prices {:IR 1 :R 3 :O 6 :Y 10 :G 30 :B 60 :I 100 :V 200 :U 1000}
               }
   :paperwork {::title "Paperwork"
               ::waste? true
               }
   }
  )
(def goods
  "A map of goods and services to details."
  (->> goodsDefault
       ;; Initialises the following four keys
       (map (fn [[k v]]
              {k (-> v
                     (update-in [::prices] merge {})
                     (update-in [::quantity] merge {})
                     (update-in [::quantityMoved] merge {})
                     (update-in [::quantityLast] merge {})
                     )}))
       ;; Moves into a single map
       (apply merge {})
       )
  )
(def reactions
  "A map of reactions to details.
  Each entry MUST have ::title ::consumes ::produces"
  {:pgc {;; What this reaction is called
         ::title "PGC Production"
         ;; What goods are consumed in this reaction, per daycycle
         ::consumes {:paperwork 1
                    }
         ;; What goods are produced in this reaction, per daycycle
         ::produces {:pgc 40
                    }
         ;; Is this an illegal action? (For use in black market etc)
         ::illegal? false
         }
   :algaeFood {::title "Synthetic Foodstuffs"
               ::consumes {:pgc 1}
               ::produces {:algaeFood 1}
               }
   :power {::title "Electricity Production"
           ::consumes {}
           ::produces {:power 10}
           }
   :paperwork {::title "Paperwork"
               ::consumes {}
               ::produces {:paperwork 5}
               }
   :air {::title "Air Recycling"
         ::consumes {}
         ::produces {:air 10}
         }
   }
  )
(def serviceGroups
  "A map of service groups and their outputs"
  {:AF {::title "Armed Forces"
        ::reactions [:bodyguards :wmds :defence]
        }
   }
  )

;; Shared specifications
(s/def ::title (s/and string?
                      #(> (count %) 0)
                      ))
(s/def ::illegal? boolean?)

;; Specification of goods
(s/def ::goodType keyword?)
(s/def ::consumeClearance ::ss/clearance)
(s/def ::productionClearance ::ss/clearance)
(s/def ::waste? boolean?)
(s/def ::essential? boolean?)
(s/def ::clearanceSameCost? boolean?)
(s/def ::prices (s/map-of ::ss/clearance ::ss/price))

(s/def ::quantity (s/map-of ::ss/clearance ::ss/quantity))
(s/def ::quantityMoved ::quantity)
(s/def ::quantityLast ::quantity)
(s/def ::good (s/keys :req [::title

                            ;; These should be initialised at runtime
                            ::prices
                            ::quantity
                            ::quantityMoved
                            ::quantityLast
                            ]
                      :opt [;; If not here, assume :IR
                            ::consumeClearance
                            ::productionClearance

                            ::waste?
                            ::essential?
                            ::clearanceSameCost?
                            ::illegal?
                            ]
                      )
  )
(s/def ::goods (s/map-of ::goodType ::good))

;; Specifications of reactions
(s/def ::reactionType keyword?)
(s/def ::goodsList (s/map-of ::reactionType ::ss/quantity))
(s/def ::consumes ::goodsList)
(s/def ::produces ::goodsList)
(s/def ::reaction (s/keys :req [::title
                                ::consumes
                                ::produces
                                ]
                          :opt [::illegal?
                                ]
                          ))
(s/def ::reactions (s/map-of ::reactionType ::reaction))

(s/def ::economyMap (s/keys :req [::goods ::reactions]))

;; Helper Functions. All of these run on an economyMap
;; Goods functions
(defn get-good
  "Gets a good from an economy"
  [econMap good]
  {:pre [(h/valid? ::economyMap econMap)
         (h/valid? ::goodType good)]
   :post [(h/valid? (s/or :nil nil? :good ::good) %)]
   }
  (get-in econMap [::goods good])
  )
(defn get-good-price
  "Gets a good's price from an economy. Returns nil if the good doesn't exist"
  ([econMap clearance good]
   {:pre [(h/valid? ::economyMap econMap)
          (h/valid? ::goodType good)
          (h/valid? ::ss/clearance clearance)]
    :post [(h/valid? (s/or :nil nil? :price ::ss/price) %)]
    }
   ;; If clearanceSameCost?, access at :IR level
   (if-let [{prices ::prices same? ::clearanceSameCost?} (get-good econMap good)]
     (if-let [price (get prices (if same? :IR clearance))]
       ;; Price exists
       price
       ;; Price doesn't exist, but good does
       defaultGoodPrice
       )
     ;; Good doesn't exist
     nil
     )
   )
  ([econMap clearance good qty]
   {:pre [(h/valid? ::economyMap econMap)
          (h/valid? ::goodType good)
          (h/valid? ::ss/clearance clearance)
          (h/valid? ::ss/quantity qty)
          ]
    :post [(h/valid? (s/or :nil nil? :price ::ss/price) %)]
    }
   (if-let [price (get-good-price econMap clearance good)]
     (* price qty)
     nil
     )
   )
  )
(defn get-total-price
  "Given a map of goods and their quantities, calculates the current market rate at the given clearance"
  [econMap clearance goodsList]
  {:pre [(h/valid? ::economyMap econMap)
         (h/valid? ::ss/clearance clearance)
         (h/valid? ::goodsList goodsList)]
   :post [(h/valid? ::ss/price %)]}
  (->> goodsList
       ;; Get the total price of each good at the current market price
       (map (fn [[good qty]] (get-good-price econMap clearance good qty)))
       ;; Reduce to a single price
       (reduce +)
       )
  )

(defn get-reaction-profit
  "Gets the profit a reaction can make from a single production cycle at a specified clearance
  Returns nil if the specified reaction doesn't exist
  "
  [econMap reaction clearance]
  {:pre [(h/valid? ::economyMap econMap)
         (h/valid? ::reactionType reaction)
         (h/valid? ::ss/clearance clearance)]
   :post [(h/valid? (s/or :nil nil? :price ::ss/price) %)]
   }
  (if-let [{consumes ::consumes produces ::produces} (get-in econMap [::reactions reaction])]
    (- (get-total-price econMap clearance produces)
       (get-total-price econMap clearance consumes)
       )
    nil
    )
  )
(defn- begin-sell-phase-inner
  "Sets all good ::quantityLast to ::quantity"
  [goods]
  {:pre [(h/valid? ::goods goods)]
   :post [(h/valid? ::goods %)]}
  (->> goods
       (map (fn [[goodType good]]
              {goodType (assoc good ::quantityLast (::quantity good))}
              ))
       (apply merge {})
       )
  )
(defn begin-sell-phase
  "Performs all parts of the market phase"
  [econMap]
  {:pre [(h/valid? ::economyMap econMap)]
   :post [(h/valid? ::economyMap econMap)]
   }
  (-> econMap
      ;; Sell
      (update-in [::goods] begin-sell-phase-inner)
      )
  )
