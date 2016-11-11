(ns acmoi.shared.helpers
  (:require [#?(:clj clojure.spec
               :cljs cljs.spec) :as s]
            [acmoi.shared.spec :as ss]
            )
  )

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

(defn filter-keys
  "Takes a map, keeps only the keys specified"
  [m ks]
  (apply merge {} (filter (fn [[k v]] ((set ks) k)) m)))
