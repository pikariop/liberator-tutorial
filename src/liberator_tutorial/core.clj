(ns liberator-tutorial.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY]]))


(defresource secret
  :available-media-types ["text/html"]
  :exists? (fn [ctx]
    (= "tiger"
       (get-in ctx [:request :params "word"])))
  :handle-ok "You found the secret word!"
  :handle-not-found "Uh, that's the wrong word. Guess again!")

(defresource choice
  :available-media-types ["text/html"]
  :exists? (fn [ctx]
             (if-let [choice
                      (get {"1" "stone" "2" "paper" "3" "scissors"}
                           (get-in ctx [:request :params "choice"]))]
               {:choice choice}))
  :handle-ok (fn [ctx]
               (format "<html>Your choice: &quot;%s&quot;.</html>"
                       (get ctx :choice)))
  :handle-not-found (fn [ctx]
                     (format "<html>There is no value for the option &quot;%s&quot;"
                             (get-in ctx [:request :params "choice"] ""))))

(defroutes app
  (ANY "/secret" []
       secret)
  (ANY "/choice" []
       choice))

(def handler
  (-> app
      wrap-params))


