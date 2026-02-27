(ns minusthree.engine.systems
  (:require
   [minusthree.engine.input :as input]
   [minusthree.engine.loading :as loading]
   [minusthree.engine.time :as time]
   [minusthree.engine.transform3d :as t3d]
   [minusthree.gl.texture :as texture]
   [minusthree.engine.thorbox.thorbox :as thorbox]))

(def all
  [time/system
   loading/system
   texture/system
   t3d/system
   input/system

   thorbox/system])
