(ns app.ui.sunburst
  (:require cljsjs.d3
            [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]))

(def sunburst-dim {:width 790 :height 600})

(def legend-colors
  #js [#js {:key "end" :value "#C1BAA9"}
       #js {:key "home" :value "#5C8100"}
       #js {:key "srp" :value "#156B90"}
       #js {:key "pdp" :value "#0F8C79"}
       #js {:key "generic" :value "#95A17E"}
       #js {:key "phone_lead" :value "#F2DA57"}
       #js {:key "email_lead" :value "#BD2D28"}
       #js {:key "media" :value "#684664"}
       #js {:key "lead_media_div" :value "#8E6C8A"}
       #js {:key "floorplans" :value "#BA5F06"}
       #js {:key "lead_floor_plans" :value "#E6842A"}
       #js {:key "login" :value "#A0B700"}
       #js {:key "register" :value "#A0B700"}
       #js {:key "social_login" :value "#A0B700"}
       #js {:key "lead_property_listings" :value "#E3BA22"}
       #js {:key "lead_property_thank_you_page" :value "#E3BA22"}
       #js {:key "lead_property_spotlight" :value "#E3BA22"}
       #js {:key "lead_nearby_properties" :value "#E3BA22"}
       #js {:key "lead_property_overview" :value "#E3BA22"}
       #js {:key "lead_contact_property_bottom" :value "#E3BA22"}])

(let [width   200
      height  30
      spacing 3
      radius  3]
  (defn render-legend
    []
    ;; compute the width and (cumulative) height of the Legend
    (let [legend (-> js/d3
                     (.select "#legend")
                     (.append "svg:svg")
                     (.attr "width" width)
                     (.attr "height" (-> js/d3
                                         (.keys legend-colors)
                                         (.-length)
                                         (* (+ height spacing))) width))

          group (-> legend
                    (.selectAll "g")
                    (.data legend-colors)
                    (.enter)
                    (.append "svg:g")
                    (.attr "transform" (fn [d i]
                                         (let [dy (* i (+ height spacing))]
                                           (str "translate(0," dy ")")))))]

     ;; build & style legend rectangles
     (-> group
         (.append "svg:rect")
         (.attr "rx" radius)
         (.attr "ry" radius)
         (.attr "width" width)
         (.attr "height" height)
         (.style "fill" (fn [d] (.-value d))))

     ;; add legend text
     (-> group
         (.append "svg:text")
         (.attr "x" (/ width 2))
         (.attr "y" (/ height 2))
         (.attr "dy" "0.35em")
         (.attr "text-anchor" "middle")
         (.text (fn [d] (.-key d)))))))

(defn render-sunburst
  [component props]
  (render-legend))

(defui ^:once Path
  static uc/InitialAppState
  (initial-state [clz params]
                 (merge {:path ["home" "srp" "pdp" "end"] :visits 200}
                        params))
  static om/Ident
  (ident [this props] [:path/by-id (:path props)])
  static om/IQuery
  (query [this] [:path :visits]))

(defui ^:once Sunburst
  static uc/InitialAppState
  (initial-state
   [clz params]
   {:paths
    [(uc/initial-state Path {})
     (uc/initial-state Path {:path ["home" "srp" "end"] :visits 140})
     (uc/initial-state Path {:path ["srp" "pdp" "end"] :visits 120})
     (uc/initial-state Path {:path ["srp" "end"] :visits 240})]})
  static om/IQuery
  (query [this] [{:paths (om/get-query Path)}])
  Object
  (componentDidMount [this] (render-sunburst this (om/props this)))
  (shouldComponentUpdate [this next-props next-state] false)
  (componentWillReceiveProps [this props] (render-sunburst this props))
  (render
   [this]
   (dom/svg #js {:style   #js {:backgroundColor "rgb(240,240,240)"}
                 :width   (:width  sunburst-dim)
                 :height  (:height sunburst-dim)
                 :viewBox "0 0 1000 1000"})))
(def ui-sunburst (om/factory Sunburst))


(defn tag->filename
  [t]
  (case t
    :srp "/data/srp.csv"
    :pdp "/data/pdp.csv"
    "/data/pdp.csv"))

(defui ^:once SunburstTab
  static uc/InitialAppState
  (initial-state [clz params] {:id :pdp
                               :type :sunburst-tab
                               :data [] #_(uc/initial-state Sunburst {})
                               :content :pdp})
  static om/Ident
  (ident [this {id :id :as props}] [:sunburst-tab id])
  static om/IQuery
  (query [this] [:id :type :content {:data (om/get-query Sunburst)}])

  Object

  (componentWillUnmount
   [this]
   (js/console.log "will un-mount"))

  (componentWillReceiveProps
   [this next-props]
   (js/console.log "will recv. props" next-props)
   (let [curr-props (om/props this)
         {:keys [content]} next-props])
   this)

  (componentWillUpdate
   [this next-props next-state]
   (js/console.log "will update" next-props next-state)
   this)

  (componentDidMount  ;; works only if `sequences.js` loaded prior to `app.js`
   [this]
   (js/console.log "Make it so")
   (js/console.log "with props: " (om/props this))
   (js/makeItSo (tag->filename (:content (om/props this)))))

  (render [this]
          (let [{:keys [data]} (om/props this)]
            (dom/div #js {:id "sunburst-tab"}

                     (dom/div #js {:id "main"}
                              (dom/div #js {:id "sequence"})
                              (dom/div #js {:id "chart"}
                                       (dom/div #js {:id "explanation" :style #js {:visibility "hidden"}}
                                                (dom/span #js {:id "percentage"})
                                                (dom/br nil)
                                                "of visits begin with this sequence of pages")
                                       ;; (ui-sunburst data)
                                       ))

                     ;; Sunburst legend
                     (dom/div #js {:id "sidebar"}
                              (dom/div #js {:id "legend"}))))))
(def ui-tab (om/factory SunburstTab {:keyfn (juxt :type :id)}))
