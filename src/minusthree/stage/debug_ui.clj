(ns minusthree.stage.debug-ui
  (:require
   [clojure.spec.alpha :as s]
   [fastmath.vector :as v]
   [minusthree.engine.macros :refer [insert!]]
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
