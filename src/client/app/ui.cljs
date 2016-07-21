(ns app.ui
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            yahoo.intl-messageformat-with-locales
            [app.ui.sunburst :as sunburst :refer [SunburstTab]]
            [app.ui.data-nav :as data-nav :refer [DataTab]]
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]))

(defui ^:once TabManager
  static uc/InitialAppState
  (initial-state [clz params] (uc/initial-state SunburstTab {}))
  static om/Ident
  (ident [this {:keys [type id] :as props}] [type id])
  static om/IQuery
  (query [this] {:data-tab     (om/get-query DataTab)
                 :sunburst-tab (om/get-query SunburstTab)})
  Object
  (render [this]
          (let [props (om/props this)]
            (case (:type props)
              :data-tab     (data-nav/ui-tab props)
              :sunburst-tab (sunburst/ui-tab props)
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
                     (dom/button #js {:onClick #(om/transact! this '[(app/choose-tab {:tab :sunburst-tab})])} "Sunburst")
                     (dom/button #js {:onClick #(om/transact! this '[(app/choose-tab {:tab :data-tab})])}     "Data")
                     (ui-tabs tabs)))))
