(ns app.core
  (:require
    app.mutations
    [untangled.client.data-fetch :as df]
    [untangled.client.core :as uc]
    [app.ui.todo :as todo]
    [app.ui.data-nav :as nav]
    [app.ui.data-viz :as viz]
    [om.next :as om]))

(defonce app (atom (uc/new-untangled-client
                     :started-callback
                     (fn [{:keys [reconciler]}]
                       (df/load-data reconciler
                                     [{:all-tables (om/get-query nav/DataTable)}
                                      {:all-viz    (om/get-query viz/DataViz)}]
                                     :post-mutation 'fetch/initial-load)))))
