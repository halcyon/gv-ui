(ns app.core
  (:require
    app.mutations
    [untangled.client.data-fetch :as df]
    [untangled.client.core :as uc]
    [app.ui.todo :as todo]
    [app.ui.data-nav :as data]
    [om.next :as om]))

(defonce app (atom (uc/new-untangled-client
                     :started-callback
                     (fn [{:keys [reconciler]}]
                       (df/load-data reconciler [{:all-tables (om/get-query data/DataTable)}]
                                     :post-mutation 'fetch/data-loaded)))))
