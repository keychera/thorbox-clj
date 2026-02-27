(ns minusthree.engine.input
  (:require
   [clojure.spec.alpha :as s]
   [minusthree.engine.world :as world]
   [minusthree.platform.jvm.input.scroll :as scroll]
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
   {}))

(def system
  {::world/rules #'rules})
