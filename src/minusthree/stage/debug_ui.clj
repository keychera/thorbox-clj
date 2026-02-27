(ns minusthree.stage.debug-ui
  (:require
   [clojure.spec.alpha :as s]
   [fastmath.vector :as v]
   [minusthree.engine.macros :refer [insert! s->]]
   [minusthree.engine.time :as time]
   [minusthree.engine.transform2d :as t2d]
   [minusthree.engine.utils :as utils]
   [minusthree.engine.world :as world]
   [odoyle.rules :as o]))

;; public

(defn toggle-fps-panel [world]
  (let [render? (utils/query-one world ::fps-panel)]
    (o/insert world ::fps-counter ::render? (not render?))))

;; internal rules

(s/def ::render? boolean?)
(s/def ::fps-value string?)

(s/def ::last-time number?)
(s/def ::frames number?)

(defn after-refresh [world {tt ::time/total}]
  (o/insert world ::fps-counter
            {::render? true
             ::fps-value "000 fps"
             ::last-time tt
             ::frames 0
             ::t2d/position (v/vec2 25 690)}))

(def rules
  (o/ruleset
   {::fps-panel
    [:what
     [::fps-counter ::render? true]
     [::fps-counter ::fps-value fps-value]
     [::fps-counter ::t2d/position position]]

    ::update-fps-value
    [:what
     [::time/now ::time/total tt]
     [::fps-counter ::last-time last-time {:then false}]
     [::fps-counter ::frames frames {:then false}]
     [::fps-counter ::fps-value fps-value {:then false}]
     :then
     (if (> (- tt last-time) 1e3)
       (insert! ::fps-counter {::last-time tt ::frames 0 ::fps-value (format "%03d fps" frames)})
       (insert! ::fps-counter {::frames (inc frames)}))]}))

(def system
  {::world/after-refresh #'after-refresh
   ::world/rules #'rules})


(comment
  (s/def ::move-direction #{:up :down :left :right})

  (def anewrules
    (o/ruleset
     {::horse-render-data
      [:what
       [::horse ::t2d/position position]
       [::horse ::t2d/scale scale]]

      ::horse-movement
      [:what
       [::input-global ::move-direction direction]
       [::horse ::t2d/position position {:then false}]
       :then
       (let [magnitude 10
             move-value
             (case direction
               :right (v/vec2 magnitude 0.0)
               :left  (v/vec2 (- magnitude) 0.0)
               :up    (v/vec2 0.0 (- magnitude))
               :down  (v/vec2 0.0 magnitude))]
         (s-> session
              (o/retract ::input-global ::move-direction)
              (o/insert ::horse ::t2d/position (v/add move-value position))))]}))

  (-> (::world/this (world/init-world {} [{::world/rules anewrules}]))
      (o/insert ::horse {::t2d/position (v/vec2 1.0 0.0)})
      (o/insert ::horse {::t2d/scale (v/vec2 1.0 0.0)})
      (o/insert ::input-global {::move-direction :right})
      (o/fire-rules)
      (o/insert ::input-global {::move-direction :right})
      (o/fire-rules)
      (o/query-all ::horse-render-data))

  :-)
