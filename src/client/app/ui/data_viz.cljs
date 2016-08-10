(ns app.ui.data-viz
  (:require cljsjs.d3
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]
            [cljs.pprint :as pp]))

(def dim {:width 700 :height 700})



(comment
  (reduce (fn [acc [path num]]
            (let [{:keys [acc]}
                  (reduce (fn [{:keys [acc prior-path]} next-path-seg]
                            (let [curr-path (conj prior-path next-path-seg)]
                              {:prior-path curr-path
                               :acc (update-in acc
                                               (conj curr-path :count)
                                               (fnil +' 0)
                                               num)}))
                          {:acc acc :prior-path []}
                          (concat ["root"] path))]
              acc))
          {}
          [[["a" "b" "c" "d" "end"] 20]
           [["a" "c" "end"]         10]
           [["a" "end"]             15]
           [["a" "a" "b" "c" "end"] 30]
           [["a" "b" "c" "e" "end"] 35]
           [["b" "end"]             50]])


  (reduce (fn [acc [path num]]
            (let [{:keys [acc]}
                  (reduce (fn [{:keys [acc prior-path]} next-path-seg]
                            (let [curr-path (conj prior-path next-path-seg)]
                              {:prior-path curr-path
                               :acc (update-in acc
                                               (conj curr-path :count)
                                               (fnil +' 0)
                                               num)}))
                          {:acc acc :prior-path []}
                          (concat ["root"] path))]
              acc))
          {}
          [[["a" "b" "c" "d" "end"] 20]
           [["a" "c" "end"]         10]
           [["a" "end"]             15]
           [["a" "a" "b" "c" "end"] 30]
           [["a" "b" "c" "e" "end"] 35]
           [["b" "end"]             50]])



  [[["a" "b" "end"] 20]
   [["a" "c" "end"] 10]
   [["a" "end"]     15]
   [["a" "b" "c" "end"] 30]
   [["b" "b" "c" "end"] 35]
   [["b" "end"]         50]])







(defn ingest-row
  "Updates the accumulator with the row."
  [acc row]
  (let [{:keys [count path]} row]
    ))

(defn hiera
  []
  (let [data [{:count 10 :path ["a" "b" "c" "d" "end"]}
              {:count 20 :path ["a" "c" "end"]}
              {:count 30 :path ["a" "end"]}
              {:count 40 :path ["a" "a" "b" "c" "end"]}
              {:count 15 :path ["a" "b" "c" "e" "end"]}
              {:count 5  :path ["b" "end"]}]]

    (reduce (fn [acc {:as row :keys [count path]}]
              (ingest-row acc row))
            {:name "root" :children []}
            data)))


(defn render-sunburst-2
  [component props]
  (js/console.log "render-sunburst-2" {:comp component :props props})
  (let [svg (-> js/d3 (.select (dom/node component)))
        data #js [] #_(clj->js (:squares props))
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


(defn render-sunburst
  [component props]
  (js/console.log "render-sunburst" {:comp component :props props})
  (let [svg (-> js/d3 (.select (dom/node component)))
        data (clj->js (:data props))
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

;; mapping of viz. type -> rendering procedure
(def render-fn
  {:sunburst render-sunburst})

(defui ^:once Sequence
  static om/Ident
  (ident [this props] [:sequence/by-id (:id props)])
  static om/IQuery
  (query [this] [:id :path :count]))

(defui ^:once DataViz
  static om/Ident
  (ident [this props] [:data-viz/by-id (:id props)])
  static om/IQuery
  (query [this] [:id :type {:data (om/get-query Sequence)}])
  Object
  (shouldComponentUpdate [this next-props next-state] false)
  (componentDidMount
   [this]
   (let [{t :type :as props} (om/props this)
         _ (js/console.log [:did-mount {:type t :id (:id props)}])
         ]
     ((render-fn t) this props)))

  (componentWillReceiveProps
   [this props]
   (let [{t :type} props]
     ((render-fn t) this props)))

  (render
   [this]
   (let [{id :id
          t :type} (om/props this)
         class-name (str "viz-" (name t))]
     (dom/div #js {:className class-name}
              (dom/svg #js {:style   #js {:backgroundColor "rgb(240,240,240)"}
                            :width   (:width dim)
                            :height  (:height dim)
                            :viewBox "0 0 1000 1000"})))))

(def ui-data-viz (om/factory DataViz {:keyfn :id}))

(defui ^:once DataVizTab
  static uc/InitialAppState
  (initial-state [clz params] {:id 1
                               :type :data-viz-tab
                               :content []})
  static om/Ident
  (ident [this props] [:data-viz-tab (:id props)])
  static om/IQuery
  (query [this] [:id :type {:content (om/get-query DataViz)}])
  Object
  (render [this]
          (let [{:keys [content]} (om/props this)]
            (dom/div #js {:id "data-viz-tab"}
                     (map ui-data-viz content)))))

(def ui-tab (om/factory DataVizTab {:keyfn (juxt :type :id)}))
