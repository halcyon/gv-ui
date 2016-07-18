(ns app.ui
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            yahoo.intl-messageformat-with-locales
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]))

(defui ^:once Item
  static uc/InitialAppState
  (initial-state [clz {:keys [id label]}] {:id id :label label})
  static om/IQuery
  (query [this] [:id :label])
  static om/Ident
  (ident [this {:keys [id]}] [:items/by-id id])
  Object
  (render [this]
    (let [{:keys [label]} (om/props this)]
      (dom/li nil label))))

(def ui-item (om/factory Item {:keyfn :label}))

(defui ^:once MyList
  static uc/InitialAppState
  (initial-state [clz params] {:title             "Initial List"
                               :ui/new-item-label ""
                               :items             []})
  static om/IQuery
  (query [this] [:ui/new-item-label :title {:items (om/get-query Item)}])
  static om/Ident
  (ident [this {:keys [title]}] [:lists/by-title title])
  Object
  (render [this]
    (let [{:keys [title items ui/new-item-label] :or {ui/new-item-label ""}} (om/props this)]
      (dom/div nil
        (dom/h4 nil title)
        (dom/input #js {:value    new-item-label
                        :onChange (fn [evt] (m/set-string! this :ui/new-item-label :event evt))})
        (dom/button #js {:onClick #(do
                                    (m/set-string! this :ui/new-item-label :value "")
                                    (om/transact! this `[(app/add-item {:id ~(om/tempid) :label ~new-item-label})
                                                         (untangled/load {:query         [{:all-items ~(om/get-query Item)}]
                                                                          :post-mutation fetch/items-loaded})
                                                         ]))} "+")
        (dom/ul nil (map ui-item items))))))

(def ui-list (om/factory MyList))

(defui ^:once TodoTab
  static uc/InitialAppState
  (initial-state [clz params]
                 {:id 1
                  :type :todo-tab
                  :lists [(uc/initial-state MyList {})]})
  static om/Ident
  (ident [this {id :id :as props}] [:todo-tab id])
  static om/IQuery
  (query [this] [:id :type {:lists (om/get-query MyList)}])
  Object
  (render [this]
          (let [{:keys [lists]} (om/props this)]
            (dom/div nil
                     (dom/b nil (str "TodoTab lists: " (pr-str lists)))
                     (map ui-list lists)))))
(def ui-todo-tab (om/factory TodoTab {:keyfn :id}))


(defui ^:once DataPoint
  static om/Ident
  (ident [this {:keys [id] :as props}] [:point/by-id id])
  static om/IQuery
  (query [this] [:id :x :y :t])
  Object
  (render [this]
          (let [ps (om/props this)]
            (js/console.log "rendering DataPoint")
            (dom/li nil (pr-str ps)))))
(def ui-data-point (om/factory DataPoint {:keyfn :id}))

(defui ^:once DataSeries
  static uc/InitialAppState
  (initial-state [clz params]
                 {:id 1
                  :points [{:id 0 :t 0 :x 0 :y 0}
                           {:id 1 :t 1 :x 1 :y 1}
                           {:id 2 :t 2 :x 1 :y 2}
                           {:id 3 :t 3 :x 2 :y 3}
                           {:id 4 :t 4 :x 2 :y 3}
                           {:id 5 :t 5 :x 3 :y 5}]})
  static om/Ident
  (ident [this {:keys [id] :as props}] [:series/by-id id])
  static om/IQuery
  (query [this] [:id {:points (om/get-query DataPoint)}])
  Object
  (render [this]
          (let [{:keys [id points]} (om/props this)]
            (js/console.log "rendering DataSeries...")
            (js/console.log "DataSeries points: " points)
            (dom/ol nil (str "data series " id ": ")
                    (map ui-data-point points)))))
(def ui-data-series (om/factory DataSeries {:keyfn :id}))

(defui ^:once DataViz
  static uc/InitialAppState
  (initial-state [clz params] {:id 1 :series [(uc/initial-state DataSeries {})]})
  static om/Ident
  (ident [this {id :id :as props}] [:viz/by-id id])
  static om/IQuery
  (query [this] [:id {:series (om/get-query DataSeries)}])
  Object
  (render [this]
          (let [{:keys [id series]} (om/props this)]
            (js/console.log "rendering DataViz...")
            (js/console.log "DataViz props: " (om/props this))
            (dom/div nil
                     (dom/b nil (str "DataViz " id ": "))
                     (map ui-data-series series)))))

(def ui-data-viz (om/factory DataViz {:keyfn :id}))

(defui ^:once DataTab
  static uc/InitialAppState
  (initial-state [clz params] {:id 1
                               :type :data-tab
                               :widgets [(uc/initial-state DataViz {})]})
  static om/Ident
  (ident [this {id :id :as props}] [:data-tab id])
  static om/IQuery
  (query [this] [:id :type {:widgets (om/get-query DataViz)}])
  Object
  (render [this]
          (let [{:keys [widgets]} (om/props this)]
            (js/console.log "rendering DataTab...")
            (js/console.log "DataTab widgets: " widgets)
            (dom/div nil
                     (dom/b nil "DataTab")
                     (map ui-data-viz widgets)))))
(def ui-data-tab (om/factory DataTab {:keyfn :id}))

(defui ^:once TabManager
  static uc/InitialAppState
  (initial-state [clz params] (uc/initial-state TodoTab {}))
  static om/Ident
  (ident [this {:keys [type id] :as props}] [type id])
  static om/IQuery
  (query [this] {:todo-tab (om/get-query TodoTab)
                 :data-tab (om/get-query DataTab)})
  Object
  (render [this]
          (let [props (om/props this)]
            (case (:type props)
              :todo-tab (ui-todo-tab props)
              :data-tab (ui-data-tab props)
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
                     (dom/button #js {:onClick #(om/transact! this '[(app/choose-tab {:tab :todo-tab})])}
                                 "Todo")

                     (dom/button #js {:onClick #(om/transact! this '[(app/choose-tab {:tab :data-tab})])}
                                 "DataViz")
                     (ui-tabs tabs)))))
