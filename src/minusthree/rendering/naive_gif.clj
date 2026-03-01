(ns minusthree.rendering.naive-gif
  (:require
   [clojure.spec.alpha :as s]
   [fastmath.vector :as v]
   [minusthree.engine.loading :as loading]
   [minusthree.engine.raw-data :as raw-data]
   [minusthree.engine.time :as time]
   [minusthree.engine.types :as types]
   [minusthree.engine.utils :as utils :refer [raw-from-here]]
   [minusthree.engine.world :as world :refer [esse]]
   [minusthree.gl.cljgl :as cljgl]
   [minusthree.gl.gl-magic :as gl-magic]
   [minusthree.gl.texture :as texture]
   [minusthree.rendering.+render-data :as +render-data]
   [odoyle.rules :as o])
  (:import
   [org.lwjgl.opengl GL45]))

(def fbo-vs (raw-from-here "sprite-normal.vert"))
(def fbo-fs (raw-from-here "sprite.frag"))

(def horse-frames-png
  ["public/nondist/horse-0.png"
   "public/nondist/horse-1.png"
   "public/nondist/horse-2.png"
   "public/nondist/horse-3.png"
   "public/nondist/horse-4.png"
   "public/nondist/horse-5.png"])

(defn make-horse-anime [esse-id]
  (conj
   (into []
         (comp
          (map-indexed
           (fn [idx frame]
             [[idx ::texture/image (utils/get-image-from-resource frame)]
              [idx ::texture/for esse-id]]))
          cat)
         horse-frames-png)
   [esse-id ::texture/count (count horse-frames-png)]
   [esse-id ::texture/data {}]))

(defn create-naive-gif-gl []
  (let [program-info (cljgl/create-program-info-from-source fbo-vs fbo-fs)
        gl-summons     (gl-magic/cast-spell
                        [{:bind-vao :naive-gif}
                         {:buffer-data raw-data/plane3d-vertices :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :a_pos :use-shader program-info :count 3 :component-type GL45/GL_FLOAT}
                         {:buffer-data raw-data/plane3d-uvs :buffer-type GL45/GL_ARRAY_BUFFER}
                         {:point-attr :a_uv :use-shader program-info :count 2 :component-type GL45/GL_FLOAT}

                         {:unbind-vao true}])
        vao          (-> gl-summons ::gl-magic/data ::gl-magic/vao (get :naive-gif))]
    {:program-info program-info :vao vao}))


(defn horse-esse [world]
  (let [esse-id ::horse
        image-w 57
        image-h 32]
    (-> world
        (esse esse-id
              {::+render-data/data (create-naive-gif-gl)
               ::dimension (v/vec2 image-w image-h)}
              (loading/push #(make-horse-anime esse-id))))))

(defn init-fn [world _game]
  (horse-esse world))

(s/def ::dimension ::types/vec2)

(def rules
  (o/ruleset
   {::horse-anime
    [:what
     [::time/now ::time/total tt]
     [::horse ::texture/data horse-frames]
     [::horse ::dimension dimension]
     [::horse ::+render-data/data render-data]]}))

(def adhoc-horse-tint
  (float-array [0.8 0.6 0.6 0.7]))

(def adhoc-horse-pos-scale
  (float-array [-32.0 -19.0 12.0 5.0]))

(def frame-n (count horse-frames-png))
(def adhoc-animation-rate 125)

(defn render-world [world]
  (let [{:keys [tt horse-frames dimension render-data]}
        (utils/query-one world ::horse-anime)]
    (when (and (= frame-n (count horse-frames)) render-data)
      (let [{:keys [program-info vao]} render-data
            curr   (int (Math/floor (mod (/ tt adhoc-animation-rate) frame-n)))
            frame  (:gl-texture (get horse-frames curr))]
        (GL45/glUseProgram (:program program-info))
        (GL45/glBindVertexArray vao)

        (GL45/glActiveTexture GL45/GL_TEXTURE0)
        (GL45/glBindTexture GL45/GL_TEXTURE_2D frame)
        (cljgl/set-uniform program-info :u_tex 0)

        (cljgl/set-uniform program-info :u_tint adhoc-horse-tint)
        (cljgl/set-uniform program-info :u_pos_scale adhoc-horse-pos-scale)
        (cljgl/set-uniform program-info :u_res (float-array dimension))
        (GL45/glDrawArrays GL45/GL_TRIANGLES 0 6)))))

(def system
  {::world/init-fn #'init-fn
   ::world/rules #'rules})

(comment

  :-)
