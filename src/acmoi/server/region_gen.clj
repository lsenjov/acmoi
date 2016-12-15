(ns acmoi.server.region-gen
  "Details world generation"
  (:require [taoensso.timbre :as log]
            [clojure.spec :as s]

            [acmoi.shared.spec :as ss]
            [acmoi.shared.economy :as es]

            [acmoi.shared.helpers :as h]

            [acmoi.server.db :as db]
            )
  )

(def ^:private filenames
  "The file to read to get the rest of the filenames to load data from.
  The file should contain a single map, with the keys :goods, :reactions"
  "files.edn")

(defn- assoc-keywords
  "Given a map of keywords to objects, assocs the key under a :ident index in value.
  Returns the map"
  [m]
  (apply merge
         {}
         (for [[k v] m]
           {k (assoc v :ident k)}
           )
         )
  )

(defn generate-goods
  "Generates goods for a single, newly developed region and adds them as sell orders with a price of 0"
  ;; TODO Test
  [goodsMap regionId]
  {:pre [(h/valid? (s/map-of keyword? map?) goodsMap)]}
  (apply merge {}
         (for [{:keys [ident genChance genAmount] :as good} (vals goodsMap)]
           (if (< (rand) (if genChance genChance 0))
             {:price 0
              :ident ident
              :techLevel 0
              :qty (inc (rand-int (if genAmount genAmount 0)))
              :region regionId
              }
             nil
             )
           )
         )
  )

(defn generate-region
  "Recursively generates empty regions from a vector of levels.
  Parent is a string. If nil, means it is the universe.
  Saves each generated region into the database.
  Returns the universe"
  ([typeVec goodsMap parent level]
   {:pre [(h/valid? (s/coll-of map?) typeVec)
          (h/valid? (s/nilable string?) parent)
          (h/valid? (s/nilable integer?) level)
          ]
    }
   (let [{:keys [title]} (first typeVec)
         {:keys [numMin numMax transportCost]} (second typeVec)
         id (db/get-new-id)
         region {:_id id
                 :level level
                 :title title
                 :parent parent
                 :children (if (= 0 (count (rest typeVec)))
                             ;; This is the lowest level
                             nil
                             ;; Still more levels to go
                             (into []
                                   (map :_id
                                        ;; n is a range of numbers between numMin and numMax items
                                        (for [n (range 0
                                                       (+ numMin
                                                          (rand-int (- (inc numMax)
                                                                       numMin))))
                                              ]
                                          (generate-region (rest typeVec)
                                                           goodsMap
                                                           id
                                                           (inc level))
                                          )
                                        )
                                   )
                             )
                 :goods (if (= 0 (count (rest typeVec)))
                          ;; Lowest level
                          (generate-goods goodsMap id)
                          ;; Not lowest level
                          {}
                          )
                 :transportCost transportCost
                 }
         ]
     (db/upsert-region region)
     region
     )
   )
  ([typeVec goodsMap]
   (generate-region (concat [{:title "Universe"}] typeVec)
                    goodsMap
                    nil
                    0)
   )
  )

(defn reset-and-gen-new-world
  "WARNING: WILL ERASE ALL DATA
  Resets the database, and creates a new universe to simulate from config files.
  If it can't find the config files, won't reset anything.
  Returns truthy on success, else nil" ;; TODO return goods/reaction map on success?
  []
  (try (let [files (clojure.edn/read-string (slurp filenames))
             goods (->> files :goods slurp clojure.edn/read-string assoc-keywords)
             reactions (->> files :reactions slurp clojure.edn/read-string assoc-keywords)
             levels (->> files :levels slurp clojure.edn/read-string)
             ]
         ;; Reset database
         (db/reset-database)

         ;; Generate regions
         (generate-region levels goods)

         ;; Add items to database
         (doall (for [[_ v] goods] (db/upsert-good v)))
         (doall (for [[_ v] reactions] (db/upsert-reaction v)))
         )
       (catch Exception e (do (log/error "Could not complete generation. Error:" e) nil))
       )
  )

(reset-and-gen-new-world)
