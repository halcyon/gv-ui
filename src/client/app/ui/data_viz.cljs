(ns app.ui.data-viz
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            yahoo.intl-messageformat-with-locales
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]))

(defui ^:once DataPoint
  static om/Ident
  (ident [this {:keys [id] :as props}] [:point/by-id id])
  static om/IQuery
  (query [this] [:id :x :y :t])
  Object
  (render [this]
          (let [ps (om/props this)]
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
            (dom/div nil
                     (dom/b nil "DataTab")
                     (map ui-data-viz widgets)))))
(def ui-tab (om/factory DataTab {:keyfn :id}))
