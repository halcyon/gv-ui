(ns app.system
  (:require
    [clojure.java.io :as io]
    [clojure.data.csv :as csv]
    [clojure.string :as str]
    [untangled.server.core :as core]
    [app.api :as api]
    [app.handler.oauth2 :as oauth2]
    [bidi.bidi :as bidi]
    [om.next.server :as om]
    [taoensso.timbre :as timbre]
    [com.stuartsierra.component :as c]
    [om.next.impl.parser :as op]))

(defn logging-mutate [env k params]
  (timbre/info "Mutation Request: " k)
  (api/apimutate env k params))

(defn logging-query [{:keys [ast] :as env} k params]
  (timbre/info "Query: " (op/ast->expr ast))
  (api/api-read env k params))

(defn seed-db
  [filename]
  (with-open [in-file (io/reader filename)]
    (let [table-id        1 ;; hardcoded for now
          [header & rows] (csv/read-csv in-file)
          table-rows      (into []
                                (comp   (take 50)
                                     (map-indexed (fn [idx [path count]]
                                                {:id [table-id idx]
                                                 :path (into [] (str/split path #"-"))
                                                 :count count})))
                                rows)]
      {table-id {:id     table-id
                 :header {:id [table-id 0] :cols header}
                 :rows   table-rows}})))


(defrecord Database [items next-id]
  c/Lifecycle
  (start [this] (assoc this
                  :items   (atom [])
                  :next-id (atom 1)
                  :tables  (atom (seed-db "resources/public/mdot.csv"))))
  (stop [this] this))

(defn make-system
  []
  (core/make-untangled-server
    :config-path "config/demo.edn"
    :parser (om/parser {:read logging-query :mutate logging-mutate})
    :parser-injections #{:db}
    :components {:db (map->Database {})}
    :extra-routes {:routes   ["/" {"oauth2" {:get {"/auth"     :oauth2-auth
                                                   "/redirect" :oauth2-access
                                                   "/contacts" :contacts}}}]
                   :handlers {:oauth2-auth   oauth2/auth
                              :oauth2-access oauth2/redirect-handler
                              :contacts      oauth2/contacts}}))
