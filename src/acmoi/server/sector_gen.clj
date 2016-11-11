(ns acmoi.server.sector-gen
  (:require [taoensso.timbre :as log]
            [clojure.spec :as s]

            [acmoi.shared.spec :as ss]
            )
  )

(def default-sector
  "The default setup of an empty sector. Useful for testing."
  {:zone "ABC"
   ;; The next id to use
   :citizenId 1
   :citizens {}}
  )

(defn- generate-first-name
  "Generates a first name"
  [gender]
  {:pre [(s/assert ::ss/gender gender)]
   :post [(s/assert ::ss/fName %)]}
  "TestName" ;; TODO
  )

(defn- generate-citizen
  "Generates a brand new adult citizen for the sector"
  [zone citizenId]
  {:post [(s/assert ::ss/citizens %)]}
  (log/trace "Generate-citizen:" zone citizenId)
  (let [gender (rand-nth [:male :female])]
    {citizenId
     {:gender gender
      :fName (generate-first-name gender)
      :clearance :IR
      :zone zone
      :citizenId citizenId
      :cloneNum 1
      :commendations 0
      :treason 0
      ;; TODO Remove, this is just for debugging
      :associates (vec (repeatedly (rand-int 20) #(rand-int 1000)))
      }
     }
    )
  )

(defn add-new-citizen
  "Adds a number of generated citizens to a sector, and increments the next id.
  If n not supplied, defaults to 1 more citizen"
  ([{:keys [citizenId zone] :as sector} n]
   {:pre [(s/assert ::ss/sector sector)]
    :post [(s/assert ::ss/sector %)]}
   (log/trace "add-new-citizen. CitizenId:" citizenId "Zone:" zone "n:" n)
   (let [nextId (+ citizenId n)]
     (-> sector
         (update-in [:citizens]
                    merge
                    (apply merge
                           (map generate-citizen
                                (repeat zone)
                                (range citizenId nextId))))
         (assoc-in [:citizenId] nextId)
         )
     )
   )
  ([sector]
   (add-new-citizen sector 1))
  )

(defn generate-sector
  "Generates a random sector, promotes those required."
  [^Integer startingPop]
  (-> default-sector
      (add-new-citizen startingPop)
      )
  )
