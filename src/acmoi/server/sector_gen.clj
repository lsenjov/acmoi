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
      :associates (vec (repeatedly (rand-int 20) #(inc (rand-int 1000))))
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

(defn- modify-clearance
  "Takes a clearance, adjusts it up by the given number.
  If the number is negative, demotes"
  ([clearance ^Integer n]
   {:pre [(s/assert ::ss/clearance clearance)]
    :post [(s/assert ::ss/clearance %)]}
   (->> clearance
        ;; Convert to integer value
        (get ss/clearanceOrder)
        ;; Add the modifier
        (+ n)
        ;; Lowest is IR at 0
        (max 0)
        ;; Max is U at 8
        (min 8)
        ;; Get the keyword again
        (get ss/clearanceOrder)
        )
   )
  )


(defn- promote-citizen
  "Promotes a citizen one level, adds a bonus to their account"
  ([{:keys [] :as sector} citizenId]
   {:pre [(s/assert ::ss/sector sector)]
    :post [(s/assert ::ss/sector %)]}
   (log/trace "promote-citizen" citizenId)
   (if-let [c (get-in sector [:citizens citizenId])]
     (-> sector
         (update-in [:citizens citizenId :clearance] modify-clearance 1)
         ;; TODO Add bonus
         )
     (do
       (log/info "Citizen" citizenId "does not exist")
       sector
       )
     )
   )
  )

(defn- promote-citizens
  "Given a sector and clearance, promotes a specified number of citizens of the clearance one higher"
  [sector clearance ^Integer n]
  {:pre [(s/assert ::ss/sector sector)
         (s/assert ::ss/clearance clearance)
         (s/assert pos? n)]
   :post [(s/assert ::ss/sector sector)]}
  (->> sector
       :citizens
       ;; Keep all citizens of the specified clearance
       (filter (fn [[k {clear :clearance}]] (= clearance clear)))
       ;; Most commendable citizens first TODO check order
       (sort-by :commendations)
       ;; Get the number required
       (take n)
       ;; Now create the required functions
       (map (fn [[cid _]] (fn [sec] (promote-citizen sec cid))))
       ;; Put it all together
       (apply comp)
       ;; Now apply it to the sector
       (#(% sector))
       )
  )

(defn generate-sector
  "Generates a random sector, promotes those required."
  [^Integer startingPop]
  (-> default-sector
      (add-new-citizen startingPop)
      (promote-citizens :IR 100)
      )
  )

