(ns minusthree.engine.systems
  (:require
   [minusthree.engine.input :as input]
   [minusthree.engine.loading :as loading]
   [minusthree.engine.thorbox.thorbox :as thorbox]
   [minusthree.engine.time :as time]
   [minusthree.gl.texture :as texture]
   [minusthree.stage.debug-ui :as debug-ui]))

(def all
  [time/system
   loading/system
   texture/system
   input/system

   debug-ui/system

   thorbox/system])
