(ns minusthree.engine.input
  (:require
   [clojure.spec.alpha :as s]
   [minusthree.engine.macros :refer [insert!]]
   [minusthree.engine.world :as world]
   [minusthree.platform.jvm.input.scroll :as scroll]
   [minusthree.platform.jvm.sdl3-inputs :as inp]
   [minusthree.stage.debug-ui :as debug-ui]
   [odoyle.rules :as o]))

(defn init [game]
  (assoc game ::input-events nil))

(defn queue [game input-events]
  (assoc game ::input-events input-events))

(defn mouse-scroll [world factor]
  (o/insert world ::global ::zoom-change factor))

(defn input-zone [game]

  (if-let [new-input (::input-events game)]
    (cond-> (dissoc game ::input-events)
      (::scroll/yoffset new-input) (update ::world/this mouse-scroll (::scroll/yoffset new-input)))
    game))

(s/def ::zoom-change number?)

(def rules
  (o/ruleset
   {::input
    [:what
     [::inp/input ::inp/p ::inp/keydown]
     [::debug-ui/fps-counter ::debug-ui/render? render? {:then false}]
     :then
     (insert! ::debug-ui/fps-counter ::debug-ui/render? (not render?))]}))

(def system
  {::world/rules #'rules})
