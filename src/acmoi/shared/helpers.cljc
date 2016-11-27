(ns acmoi.shared.helpers
  (:require [#?(:clj clojure.spec
               :cljs cljs.spec) :as s]
            [taoensso.timbre :as log]
            [acmoi.shared.spec :as ss]
            )
  )

(defn uuid
  "Generates a random uuid"
  []
  (str (java.util.UUID/randomUUID)))

(defn parse-int
  "Parses a string to an integer, or nil if fail"
  [^String n]
  {:post [(s/valid?
            (s/or :nil nil?  :int integer?)
            %)]}
  #?(:clj
     (try (Integer/parseInt n)
          (catch NumberFormatException e nil)
          )
     :cljs n
     )
  )

(defn get-citizens-by-clearance
  "Gets all citizens of a certain clearance"
  [sector clearance]
  {:pre [(s/assert ::ss/sector sector)
         (s/assert ::ss/clearance clearance)]
   :post [(s/assert ::ss/citizens %)]}
  (->> sector
       :citizens
       ;; Keep all citizens of the specified clearance
       (filter (fn [[k {clear :clearance dead? :dead?}]]
                 (and (= clearance clear)
                      (not dead?)
                      )
                 )
               )
       (apply merge {})
       )
  )

(defn filter-keys
  "Takes a map, keeps only the keys specified"
  [m ks]
  (apply merge {} (filter (fn [[k v]] ((set ks) k)) m)))

(defn get-price
  "Gets the price of a good"
  [{goods :goods :as sector} clearance goodType]
  {:pre [(s/assert ::ss/sector sector)
         (s/assert ::ss/goodType goodType)
         (s/assert ::ss/clearance clearance)]
   :post [(s/assert ::ss/price %)]}
  (log/trace "get-price of" clearance goodType)
  (if-let [p (get-in goods [goodType clearance :price])]
    p
    ss/defaultGoodPrice
    )
  )
(defn calculate-profitability
  "Calculates how profitable daily production of a good is at current market prices"
  [{goods :goods :as sector} clearance reactionType]
  {:pre [(s/assert ::ss/sector sector)
         (s/assert ::ss/reactionType reactionType)
         (s/assert ::ss/clearance clearance)]
   :post [(s/assert ::ss/price %)]}
  (log/trace "calculate-profitability of" clearance reactionType)
  (let [cost (reduce + (for [[goodType qty] (get-in ss/reactions [reactionType :consumes])]
                             (* qty (get-price sector clearance goodType))))
        sell (reduce + (for [[goodType qty] (get-in ss/reactions [reactionType :produces])]
                         (* qty (get-price sector clearance goodType))))
        ]
    (- sell cost)
    )
  )
