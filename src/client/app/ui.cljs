(ns app.ui
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [app.ui.srp :as srp :refer [SRPTab]]
            [app.ui.sunburst :as sunburst :refer [SunburstTab]]
            [app.ui.data-nav :as data-nav :refer [DataTab]]
            [app.ui.data-viz :as data-viz :refer [DataVizTab]]
            [app.ui.todo :as todo :refer [TodoTab]]
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]))

 (defui ^:once TabManager
  static uc/InitialAppState
  (initial-state [clz params] (uc/initial-state SRPTab {}))
  static om/Ident
  (ident [this {:keys [type id] :as props}] [type id])
  static om/IQuery
  (query [this] {:todo-tab     (om/get-query TodoTab)
                 :data-tab     (om/get-query DataTab)
                 :sunburst-tab (om/get-query SunburstTab)
                 :srp-tab      (om/get-query SRPTab)
                 :data-viz-tab (om/get-query DataVizTab)})
  Object
  (render [this]
          (let [props (om/props this)]
            (js/console.log "TabManager switching tab, provided props: " props)
            (case (:type props)
              :todo-tab     (todo/ui-tab props)
              :data-tab     (data-nav/ui-tab props)
              :sunburst-tab (sunburst/ui-tab props)
              :srp-tab      (srp/ui-tab props)
              :data-viz-tab (data-viz/ui-tab props)
              (dom/b nil "NO TAB")))))


(def ui-tabs (om/factory TabManager))

(defui ^:once Root
  static uc/InitialAppState
  (initial-state [clz params] {:ui/react-key "start"
                               :tabs         (uc/initial-state TabManager {})})
  static om/IQuery
  (query [this] [:ui/react-key :ui/loading-data {:tabs (om/get-query TabManager)}])
  Object
  (render [this]
          (let [{:keys [react-key ui/loading-data tabs]} (om/props this)]
            (dom/div #js {:key react-key}
                     ;; navigation buttons
                     (dom/div #js {:id "tab-header"}
                              (dom/div #js {:id "header-title"}
                                      (cond-> "Mobile-AG Sequence Data"
                                        loading-data (str " (Loading data...)")))
                              (dom/button
                               #js {:className "nav-button"
                                    :onClick   #(om/transact!
                                                 this '[(app/choose-tab {:tab :srp-tab :content :srp})])} "SRP")
                              (dom/button
                               #js {:className "nav-button"
                                    :onClick   #(om/transact!
                                                 this '[(app/choose-tab {:tab :sunburst-tab :content :pdp})])} "PDP")


                              (dom/button
                               #js {:className "nav-button"
                                    :onClick   #(om/transact!
                                                 this '[(app/choose-tab {:tab :data-viz-tab})])} "Viz")


                              #_(dom/button
                               #js {:className "nav-button"
                                    :onClick   #(om/transact!
                                                 this '[(app/choose-tab {:tab :data-tab})])} "Data")

                              #_(dom/button
                               #js {:className "nav-button"
                                    :onClick   #(om/transact!
                                                 this `[(app/choose-tab {:tab :todo-tab})
                                                        (untangled/load
                                                         {:query [{:all-items ~(om/get-query todo/Item)}]
                                                          :post-mutation fetch/items-loaded})])}"Todo"))


                     ;; content
                     (ui-tabs tabs)))))
