(ns acmoi.shared.helpers
  (:require [#?(:clj clojure.spec
               :cljs cljs.spec) :as s]
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
