(ns app.ui.d3
  (:require cljsjs.d3
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]
            [app.ui.data-nav :refer [ui-data-series]]
            [cljs.pprint :as pp]))


(defn random-square
  []
  {:id    (rand-int 10000000)
   :x     (rand-int 900)
   :y     (rand-int 900)
   :size  (+ 50 (rand-int 300))
   :color (case (rand-int 5)
            0 "yellow"
            1 "green"
            2 "orange"
            3 "blue"
            4 "black")})

(defui ^:once Square
  static uc/InitialAppState
  (initial-state [clz params] (random-square))
  static om/Ident
  (ident [this {id :id :as props}] [:square/by-id id])
  static om/IQuery
  (query [this] [:id :x :y :size :color]))


(defn render-squares
  [component props]
  (let [svg (-> js/d3 (.select (dom/node component)))
        data (clj->js (:squares props))
        selection (-> svg
                    (.selectAll "rect")
                    (.data data (fn [d] (.-id d))))]
    (-> (.enter selection)
      (.append "rect")
      (.style "fill" (fn [d] (.-color d)))
      (.attr "x" "0")
      (.attr "y" "0")
      .transition
      (.attr "x" (fn [d] (.-x d)))
      (.attr "y" (fn [d] (.-y d)))
      (.attr "width" (fn [d] (.-size d)))
      (.attr "height" (fn [d] (.-size d))))
    (-> selection
      .exit
      .transition
      (.style "opacity" "0")
      .remove)
    false))

(defui ^:once D3Thing
  static uc/InitialAppState
  (initial-state [clz params] {:squares [(uc/initial-state Square {})
                                         (uc/initial-state Square {})
                                         (uc/initial-state Square {})
                                         (uc/initial-state Square {})]})
  static om/IQuery
  (query [this] [{:squares (om/get-query Square)}])
  Object
  (componentDidMount [this] (render-squares this (om/props this)))
  (shouldComponentUpdate [this next-props next-state] false)
  (componentWillReceiveProps [this props] (render-squares this props))
  (render
   [this]
   (dom/svg #js {:style   #js {:backgroundColor "rgb(240,240,240)"}
                 :width   200
                 :height 200
                 :viewBox "0 0 1000 1000"})))

(def d3-ui-component (om/factory D3Thing))

(defui ^:once D3Tab
  static uc/InitialAppState
  (initial-state [clz params] {:id 1
                               :type :d3-tab
                               :data (uc/initial-state D3Thing {})})
  static om/Ident
  (ident [this {id :id :as props}] [:d3-tab id])
  static om/IQuery
  (query [this] [:id :type {:data (om/get-query D3Thing)}])
  Object
  (render [this]
          (let [{:keys [data]} (om/props this)]
            (dom/div nil
                     (dom/b nil "D3Tab")
                     (dom/button #js {:onClick #(om/transact! this '[(d3/add-square)])} "Add Random Square")
                     (dom/button #js {:onClick #(om/transact! this '[(d3/rem-square {:id :all})])} "Clear")
                     (d3-ui-component data)
                     (dom/pre nil (with-out-str (pp/print-table (:squares data))))))))
(def ui-tab (om/factory D3Tab (juxt :type :id)))
