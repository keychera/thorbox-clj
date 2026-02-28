(ns minusthree.rendering.atlas
  (:require
   [clojure.spec.alpha :as s]
   [clojure.walk :as walk]
   [minusthree.engine.loading :as loading]
   [minusthree.engine.macros :refer [vars->map]]
   [minusthree.engine.raw-data :as raw-data]
   [minusthree.engine.utils :as utils :refer [raw-from-here]]
   [minusthree.engine.world :as world :refer [esse]]
   [minusthree.gl.cljgl :as cljgl]
   [minusthree.gl.gl-magic :as gl-magic]
   [minusthree.gl.texture :as texture]
   [minusthree.rendering.+render-data :as +render-data]
   [odoyle.rules :as o])
  (:import
   [org.lwjgl.opengl GL45]))

(defn transform-entry [item]
  (if (and (vector? item) (= (count item) 2))
    (let [[k v] item]
      (cond
        (#{:x :y :height :width} k)
        [k (Integer/parseInt v)]
        :else item))
    item))

(defn load-atlas-meta-xml [path]
  (let [meta-xml (utils/get-xml path)]
    (-> (walk/postwalk transform-entry meta-xml)
        (update :content #(map :attrs %)))))

(def fbo-vs (raw-from-here "sprite.vert"))
(def fbo-fs (raw-from-here "sprite.frag"))

(defn atlas->uv-rect [{:keys [x y width height]}]
  (let [atlas-w 1016 atlas-h 1016
        u0 (/ x atlas-w)
        v0 (/ y atlas-h)
        u1 (/ (+ x width) atlas-w)
        v1 (/ (+ y height) atlas-h)]
    [u0 v0 u1 v1]))

(def crop-arr
  [(atlas->uv-rect
    {:height 60, :width 42, :x 891, :y 650, :name "foliagePack_001.png"})
   (atlas->uv-rect
    {:height 45, :width 52, :y 958, :x 98, :name "foliagePack_019.png"})
   (atlas->uv-rect
    {:height 192, :width 151, :y 614, :x 0, :name "foliagePack_047.png"})])

(defn create-atlas-gl []
  (let [program-info (cljgl/create-program-info-from-source fbo-vs fbo-fs)
        gl-summons     (gl-magic/cast-spell
                        [{:bind-vao "atlas"}
                         {:buffer-data raw-data/plane3d-vertices :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :a_pos :use-shader program-info :count 3 :component-type GL45/GL_FLOAT}
                         {:buffer-data raw-data/plane3d-uvs :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :a_uv :use-shader program-info :count 2 :component-type GL45/GL_FLOAT}
                         {:buffer-data (float-array (flatten crop-arr)) :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :i_crop :use-shader program-info :count 4 :component-type GL45/GL_FLOAT}
                         {:vertex-attr-divisor :i_crop :use-shader program-info :divisor 1}
                         {:unbind-vao true}])
        vao          (-> gl-summons ::gl-magic/data ::gl-magic/vao (get "atlas"))]
    {:program-info program-info :vao vao}))

(s/def ::x int?)
(s/def ::y int?)
(s/def ::height int?)
(s/def ::width int?)
(s/def ::crop-data (s/keys :req-un [::x ::y ::height ::width]))

(defn init-fn [world _game]
  (-> world
      (esse ::foliage
            {::+render-data/data (create-atlas-gl)
             ::texture/count 1
             ::texture/data {}})
      (esse ::foliage-atlas
            (loading/push
             (fn []
               (let [atlas-img  (utils/get-image-from-resource "public/nondist/foliagePack_default.png")
                     atlas-meta (load-atlas-meta-xml "public/nondist/foliagePack_default.xml")]
                 [[::foliage-atlas ::texture/image atlas-img]
                  [::foliage-atlas ::texture/for ::foliage]
                  [::foliage ::meta atlas-meta]]))))))

(s/def ::meta map?)

(def rules
  (o/ruleset
   {::foliage-texture
    [:what
     [::foliage ::texture/data atlas-texture]
     [::foliage ::meta atlas-meta]
     [::foliage ::+render-data/data render-data]
     :when (seq atlas-texture)
     :then
     #_{:clj-kondo/ignore [:inline-def]}
     (def debug-var (vars->map atlas-texture atlas-meta render-data))
     (println "foliage atlas ready")]}))

(def system
  {::world/init-fn #'init-fn
   ::world/rules #'rules})

(defn render-world [world]
  (when-let [{:keys [atlas-texture render-data]} (utils/query-one world ::foliage-texture)]
    (let [{:keys [program-info vao]} render-data
          gl-texture (-> atlas-texture ::foliage-atlas :gl-texture)]
      (GL45/glUseProgram (:program program-info))
      (GL45/glBindVertexArray vao)

      (GL45/glActiveTexture GL45/GL_TEXTURE0)
      (GL45/glBindTexture GL45/GL_TEXTURE_2D gl-texture)

      (cljgl/set-uniform program-info :u_tex 0)
      (GL45/glDrawArraysInstanced GL45/GL_TRIANGLES 0 6 (count crop-arr)))))

(comment
  (require '[com.phronemophobic.viscous :as viscous])

  (viscous/inspect debug-var)
  (load-atlas-meta-xml "public/nondist/foliagePack_default.xml")

  :-)
