(ns liberator-tutorial.core
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY]]))

(def dbg-counter (atom 0))

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

(defresource dbg-resource
  :available-media-types ["text/plain"]
  :allowed-methods [:get :post]
  :handle-ok (fn [_] (format "The counter is %d" @dbg-counter))
  :post! (fn [_] (swap! dbg-counter inc)))


(defroutes app
  (ANY "/secret" []
       secret)
  (ANY "/choice" []
       choice))

(def handler
  (-> app
      wrap-params))

(defroutes dbg
  (ANY "/dbg-count" [] dbg-resource))

(def handler
  (-> dbg
      (wrap-trace :header :ui)))
