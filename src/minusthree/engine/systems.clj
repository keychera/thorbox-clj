(ns minusthree.engine.systems
  (:require
   [minusthree.engine.input :as input]
   [minusthree.engine.loading :as loading]
   [minusthree.engine.time :as time]
   [minusthree.gl.texture :as texture]
   [minusthree.platform.jvm.sdl3-inputs :as sdl3-inputs]
   [minusthree.rendering.+core :as rendering]
   [minusthree.stage.debug-ui :as debug-ui]
   [minusthree.stage.level1 :as level1]))

(def all
  [time/system
   loading/system
   texture/system

   sdl3-inputs/system
   input/system

   rendering/all

   level1/system

   debug-ui/system])
