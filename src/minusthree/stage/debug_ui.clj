(ns minusthree.stage.debug-ui
  (:require
   [clojure.spec.alpha :as s]
   [fastmath.vector :as v]
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
(s/def ::fps-value number?)

(defn after-refresh [world _game]
  (o/insert world ::fps-counter
            {::render? true
             ::fps-value 12345
             ::t2d/position (v/vec2 50 650)}))

(def rules
  (o/ruleset
   {::fps-panel
    [:what
     [::fps-counter ::render? true]
     [::fps-counter ::fps-value fps-value]
     [::fps-counter ::t2d/position position]]

    ::update-fps-value
    [:what
     [::fps-counter ::t2d/position position]
     [::fps-counter ::fps-value fps-value]]}))

(def system
  {::world/after-refresh #'after-refresh
   ::world/rules #'rules})
