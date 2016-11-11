(ns acmoi.frontend.core
  (:require [reagent.core :as reagent :refer [atom]]
            [ajax.core :refer [GET POST] :as ajax]
            [cljs.js :as cjs]
            ))

(enable-console-print!)

(println "This text is printed from src/acmoi/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

;; TODO change back to defonce when required
(def app-state (atom
                 {:citizens
                  {1 {:fName "Tray" :zone "TOR" :clearance :IR :cloneNum 3 :citizenId 1 :associates [2 3] :male? true}
                   2 {:fName "Ann" :zone "KEY" :clearance :R :cloneNum 3 :citizenId 2 :associates [1 3] :male? false}
                   3 {:fName "Tu" :zone "KEY" :clearance :R :cloneNum 3 :citizenId 2 :associates [1 2] :male? true}
                   }
                  }
                 )
  )
(def userInfo (atom {:userCitizen 1 :apiKey "tempApiKey"}))

(def ^:private colour-styles
  "Maps clearances to foreground and background colours"
  {:IR {:color "White" :background-color "Black"}
   :R {:color "White" :background-color "DarkRed"}
   }
  )

(defn- get-citizen-basic
  "Gets basic citizen info"
  [citizenId]
  (-> (ajax/easy-ajax-request
        "localhost:3000/api/citizen/basic/"
        :get
        {:params {:apiKey @userInfo :citizenId citizenId}
         :response-format :json}
        )
      js->clj
      )
  )

(defn- print-person-name
  "Prints a citizen's name from a map"
  [{:keys [fName zone clearance cloneNum]}]
  (str fName "-"
       (if (= :IR clearance)
         ""
         (str (name clearance) "-")
         )
       zone "-"
       cloneNum
       )
  )

(defn create-citizen-box
  [n]
  (let [cmap (get-in @app-state [:citizens n])
        expand (atom false)]
    (fn []
      [:div {:style (-> {:margin "2px"}
                        (#(if @expand
                            (merge % {:border "1px solid white"})
                            %))
                        (merge (get colour-styles (:clearance cmap)))
                        )
              }
       (if @expand
         [:span
          [:span {:onClick #(do (js/console.log (str "Clicked on person:" n))
                                (swap! expand not)
                                false)
                  }
           "Name: " (print-person-name cmap)] [:br]
          "Id Number:" n [:br]
          "Known associates:" [:br]
          (for [p (:associates cmap)]
            [create-citizen-box p])
          ]
         [:span {:onClick #(do (js/console.log (str "Clicked on person:" n))
                               (swap! expand not)
                               false)
                 }
          (print-person-name cmap)]
         )
       ]
      )
    )
  )
(defn hello-world []
  (let [expand (atom false)]
    (fn []
      [:div
       [:div (doall (for [n (-> @app-state :citizens keys)]
                      ^{:key n}
                      [create-citizen-box n]
                                     ))]
       [:div {:style {:color "White"}}
        ;(pr-str (get-citizen-basic 3))
        (-> "/api/citizen/basic/asdf/2/"
            (GET {:response-format :json
                  ;;:handler (fn [m] (assoc-in app-state [:citizens 3] m))
                  :handler (fn [m] (log/info m))
                  }
                 )
            ;(js->clj)
            (pr-str)
            )
        ]
       ]
      )
    )
  )

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
