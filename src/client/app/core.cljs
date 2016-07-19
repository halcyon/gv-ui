(ns app.core
  (:require
    app.mutations
    [untangled.client.data-fetch :as df]
    [untangled.client.core :as uc]
    [app.ui.todo :as todo]
    [om.next :as om]))

(defonce app (atom (uc/new-untangled-client
                     :started-callback
                     (fn [{:keys [reconciler]}]
                       (df/load-data reconciler [{:all-items (om/get-query todo/Item)}]
                                     :post-mutation 'fetch/items-loaded)))))
