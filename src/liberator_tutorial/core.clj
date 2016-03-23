(ns liberator-tutorial.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY]]))




(defroutes app
  (ANY "/secret" []
       (resource :available-media-types ["text/html"]
                 :exists? (fn [ctx]
                            (= "tiger"
                               (get-in ctx [:request :params "word"])))
                 :handle-ok "You found the secret word!"
                 :handle-not-found "Uh, that's the wrong word. Guess again!")))


(def handler
  (-> app
      wrap-params))


