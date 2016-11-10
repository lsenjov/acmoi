(ns acmoi.server.sector-gen-test
  (:require [taoensso.timbre :as log]
            [clojure.spec :as s]

            [acmoi.server.sector-gen :as sg]
            [acmoi.shared.spec :as ss]
            )
  )

(s/check-asserts true)
(-> sg/default-sector
    (sg/add-new-citizen)
    (sg/add-new-citizen)
    (sg/add-new-citizen)
    )
