(ns minusthree.engine.types
  (:require
   [clojure.spec.alpha :as s]))

(def f32-arr (Class/forName "[F"))
(s/def ::f32-arr #(instance? f32-arr %))

(s/def ::vec2 #(instance? fastmath.vector.Vec2 %))
