(ns minusthree.rendering.atlas
  (:require
   [clojure.spec.alpha :as s]
   [clojure.walk :as walk]
   [fastmath.vector :as v]
   [minusthree.engine.loading :as loading]
   [minusthree.engine.macros :refer [s-> vars->map]]
   [minusthree.engine.raw-data :as raw-data]
   [minusthree.engine.transform2d :as t2d]
   [minusthree.engine.utils :as utils :refer [raw-from-here]]
   [minusthree.engine.world :as world :refer [esse]]
   [minusthree.gl.cljgl :as cljgl]
   [minusthree.gl.gl-magic :as gl-magic]
   [minusthree.gl.shader :as shader]
   [minusthree.gl.texture :as texture]
   [minusthree.rendering.+render-data :as +render-data]
   [odoyle.rules :as o])
  (:import
   [fastmath.vector Vec2]
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
  (let [meta-xml  (utils/get-xml path)
        crop-data (walk/postwalk transform-entry meta-xml)]
    (into {}
          (map (fn [k] ((juxt :name #(dissoc % :name)) (:attrs k))))
          (:content crop-data))))

(def fbo-vs (raw-from-here "sprite.vert"))
(def fbo-fs (raw-from-here "sprite.frag"))

(defn atlas->uv-rect [{:keys [x y width height]}]
  (let [atlas-w 1016 atlas-h 1016 ;; hardcoded for now
        u0 (/ x atlas-w)
        v0 (/ y atlas-h)
        u1 (/ (+ x width) atlas-w)
        v1 (/ (+ y height) atlas-h)]
    [u0 v0 u1 v1]))

(def float-size 4)
(def max-instance 420)

(defn create-atlas-gl []
  (let [program-info (cljgl/create-program-info-from-source fbo-vs fbo-fs)
        gl-summons     (gl-magic/cast-spell
                        [{:bind-vao :atlas}
                         {:buffer-data raw-data/plane3d-vertices :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :a_pos :use-shader program-info :count 3 :component-type GL45/GL_FLOAT}
                         {:buffer-data raw-data/plane3d-uvs :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :a_uv :use-shader program-info :count 2 :component-type GL45/GL_FLOAT}

                         {:buffer-data (* float-size 4 max-instance) :buffer-type GL45/GL_ARRAY_BUFFER :usage GL45/GL_STREAM_DRAW
                          :buffer-name ::pos-scale-buffer}
                         {:point-attr :i_pos_scale :use-shader program-info :count 4 :component-type GL45/GL_FLOAT}
                         {:vertex-attr-divisor :i_pos_scale :use-shader program-info :divisor 1}

                         ;; maybe this isn't the most efficient way to do this but we cant afford to microoptimize
                         {:buffer-data (* float-size 4 max-instance) :buffer-type GL45/GL_ARRAY_BUFFER :usage GL45/GL_STREAM_DRAW
                          :buffer-name ::tint-buffer}
                         {:point-attr :i_tint :use-shader program-info :count 4 :component-type GL45/GL_FLOAT}
                         {:vertex-attr-divisor :i_tint :use-shader program-info :divisor 1}

                         {:buffer-data (* float-size 4 max-instance) :buffer-type GL45/GL_ARRAY_BUFFER :usage GL45/GL_STREAM_DRAW
                          :buffer-name ::crop-buffer}
                         {:point-attr :i_crop :use-shader program-info :count 4 :component-type GL45/GL_FLOAT}
                         {:vertex-attr-divisor :i_crop :use-shader program-info :divisor 1}

                         {:unbind-vao true}])
        vao          (-> gl-summons ::gl-magic/data ::gl-magic/vao (get :atlas))
        buffers      (-> gl-summons ::gl-magic/data ::shader/buffer)]
    {:program-info program-info :vao vao :buffers buffers}))

(s/def ::x int?)
(s/def ::y int?)
(s/def ::height int?)
(s/def ::width int?)
(s/def ::tex-name string?)
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
               (let [atlas-img       (utils/get-image-from-resource "public/nondist/foliagePack_default.png")
                     name->crop-data (load-atlas-meta-xml "public/nondist/foliagePack_default.xml")]
                 [[::foliage-atlas ::texture/image atlas-img]
                  [::foliage-atlas ::texture/for ::foliage]
                  [::foliage ::name->crop-data name->crop-data]]))))))

(defn foliage-instance
  [{:keys [tex-name pos scale]}]
  {::tex-name tex-name
   ::t2d/position (or pos (v/vec2 0.0 0.0))
   ::t2d/scale (or scale  (v/vec2 1.0 1.0))})

(s/def ::name->crop-data map?)
(s/def ::tint vector?)
(s/def ::layer int?)

(def rules
  (o/ruleset
   {::foliage-texture
    [:what
     [::foliage ::texture/data atlas-texture]
     [::foliage ::name->crop-data name->crop-data]
     [::foliage ::+render-data/data render-data]
     :when (seq atlas-texture)
     :then
     #_{:clj-kondo/ignore [:inline-def]}
     (def debug-var (vars->map atlas-texture name->crop-data render-data))
     (println "foliage atlas ready")]

    ::map-foliage-instances
    [:what
     [inst-id ::tex-name sub-tex-name]
     [::foliage ::name->crop-data name->crop-data]
     :then
     (s-> session
          (o/retract inst-id ::tex-name)
          (o/insert inst-id ::crop-data (get name->crop-data sub-tex-name)))]

    ::foliage-instances
    [:what
     [inst-id ::crop-data crop-data]
     [inst-id ::tint tint]
     [inst-id ::layer layer]
     [inst-id ::t2d/position position]
     [inst-id ::t2d/scale scale]]}))

(def system
  {::world/init-fn #'init-fn
   ;::world/after-refresh #'after-refresh
   ::world/rules #'rules})

;; hardcode
(def image-res (float-array [1016 1016]))

(defn foliage-instance->pos-scale [{:keys [^Vec2 position ^Vec2 scale crop-data]}]
  (let [scale-x (* (:width crop-data) (.x scale))
        scale-y (* (:height crop-data) (.y scale))]
    [(.x position) (.y position) scale-x scale-y]))

(defn render-world [world]
  (when-let [{:keys [atlas-texture render-data]} (utils/query-one world ::foliage-texture)]
    (let [{:keys [program-info vao buffers]} render-data
          gl-texture    (-> atlas-texture ::foliage-atlas :gl-texture)
          foliages      (->> (o/query-all world ::foliage-instances)
                             (sort-by :layer))
          pos-scale-arr (some->> (seq foliages)
                                 (into [] (map foliage-instance->pos-scale))
                                 flatten float-array)
          tint-arr      (some->> (seq foliages) (into [] (map :tint)) flatten float-array)
          rect-data     (into [] (map (comp atlas->uv-rect :crop-data)) foliages)
          instances-arr (some-> (seq rect-data) flatten float-array)]
      (when (seq foliages)
        (GL45/glUseProgram (:program program-info))
        (GL45/glBindVertexArray vao)

        (GL45/glActiveTexture GL45/GL_TEXTURE0)
        (GL45/glBindTexture GL45/GL_TEXTURE_2D gl-texture)

        (GL45/glBindBuffer GL45/GL_ARRAY_BUFFER (::pos-scale-buffer buffers))
        (GL45/glBufferData GL45/GL_ARRAY_BUFFER pos-scale-arr GL45/GL_STREAM_DRAW)

        (GL45/glBindBuffer GL45/GL_ARRAY_BUFFER (::tint-buffer buffers))
        (GL45/glBufferData GL45/GL_ARRAY_BUFFER tint-arr GL45/GL_STREAM_DRAW)

        (GL45/glBindBuffer GL45/GL_ARRAY_BUFFER (::crop-buffer buffers))
        (GL45/glBufferData GL45/GL_ARRAY_BUFFER instances-arr GL45/GL_STREAM_DRAW)

        (cljgl/set-uniform program-info :u_tex 0)
        (cljgl/set-uniform program-info :u_res image-res)
        (GL45/glDrawArraysInstanced GL45/GL_TRIANGLES 0 6 (count rect-data))))))

(comment
  (require '[com.phronemophobic.viscous :as viscous])

  (viscous/inspect debug-var)
  (load-atlas-meta-xml "public/nondist/foliagePack_default.xml")

  :-)
