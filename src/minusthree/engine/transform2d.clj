(ns minusthree.engine.transform2d 
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::position #(instance? fastmath.vector.Vec2 %))
(s/def ::scale #(instance? fastmath.vector.Vec2 %))
(s/def ::speed float?)
(s/def ::speed-stat float?)
