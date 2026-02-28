(ns minusthree.rendering.+render-data 
  (:require
   [clojure.spec.alpha :as s]
   [minusthree.gl.gl-magic :as gl-magic]
   [minusthree.gl.shader :as shader]))

(s/def ::vao ::gl-magic/vao)
(s/def ::program-info ::shader/program-info)
(s/def ::data (s/keys :req-un [::program-info ::vao]))
