(ns minusthree.engine.transform2d 
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::position #(instance? fastmath.vector.Vec2 %))
