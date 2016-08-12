(ns app.ui.srp
  (:require cljsjs.d3
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]
            [app.ui.data-nav :refer [ui-data-series]]
            [cljs.pprint :as pp]))

(defui ^:once SRPTab
  static uc/InitialAppState
  (initial-state [clz params] {:id :srp
                               :type :srp-tab
                               :content :srp})
  static om/Ident
  (ident [this {id :id :as props}] [:srp-tab id])
  static om/IQuery
  (query [this] [:id :type :content])

  Object

  (componentWillUnmount
   [this]
   (js/console.log "SRP will un-mount"))

  (componentWillReceiveProps
   [this next-props]
   (js/console.log "SRP will recv. props" next-props)
   (let [curr-props (om/props this)
         {:keys [content]} next-props])
   this)

  (componentWillUpdate
   [this next-props next-state]
   (js/console.log "will update" next-props next-state)
   this)

  (componentDidMount  ;; works only if `sequences.js` loaded prior to `app.js`
   [this]
   (js/console.log "SRP Make it so")
   (js/console.log "with props: " (om/props this))
   (js/makeItSo "/data/srp.csv"))

  (render [this]
          (let [{:keys [data]} (om/props this)]
            (dom/div #js {:id "srp-tab"}

                     (dom/div #js {:id "main"}
                              (dom/div #js {:id "sequence"})
                              (dom/div #js {:id "chart"}
                                       (dom/div #js {:id "explanation" :style #js {:visibility "hidden"}}
                                                (dom/span #js {:id "percentage"})
                                                (dom/br nil)
                                                "of visits begin with this sequence of pages")
                                       #_(ui-sunburst data)))

                     ;; Sunburst legend
                     (dom/div #js {:id "sidebar"}
                              (dom/div #js {:id "legend"}))))))
(def ui-tab (om/factory SRPTab {:keyfn (juxt :type :id)}))
