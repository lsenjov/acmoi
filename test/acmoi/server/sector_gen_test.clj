(ns acmoi.server.sector-gen-test
  (:require [taoensso.timbre :as log]
            [clojure.spec :as s]

            [acmoi.server.sector-gen :refer :all]
            [acmoi.shared.spec :as ss]
            [acmoi.shared.helpers :as helpers]
            )
  )

;; Switches for debugging
(s/check-asserts false)
(s/check-asserts true)

