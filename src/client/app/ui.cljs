(ns app.ui
  (:require [app.ui.todo :as todo :refer [TodoTab]]
            [app.ui.data-nav :as data-nav :refer [DataTab]]
            [app.ui.d3 :as d3 :refer [D3Tab]]
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            yahoo.intl-messageformat-with-locales
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]))

(defui ^:once TabManager
  static uc/InitialAppState
  (initial-state [clz params] (uc/initial-state TodoTab {}))
  static om/Ident
  (ident [this {:keys [type id] :as props}] [type id])
  static om/IQuery
  (query [this] {:todo-tab (om/get-query TodoTab)
                 :data-tab (om/get-query DataTab)
                 :d3-tab   (om/get-query D3Tab)})
  Object
  (render [this]
          (let [props (om/props this)]
            (js/console.log [:props props])
            (case (:type props)
              :todo-tab (todo/ui-tab props)
              :data-tab (data-nav/ui-tab props)
              :d3-tab   (d3/ui-tab props)
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
                     (dom/h4 nil "Header" (when loading-data " (LOADING)"))



                     ;; TODO - redesign buttons -> links in spans or smthn.
                     (dom/button #js {:onClick #(om/transact! this '[(app/choose-tab {:tab :todo-tab})])} "Todo")
                     (dom/button #js {:onClick #(om/transact! this '[(app/choose-tab {:tab :data-tab})])} "Data-Nav")
                     (dom/button #js {:onClick #(om/transact! this '[(app/choose-tab {:tab :d3-tab})])} "Data-Viz")
                     (ui-tabs tabs)))))
