(ns minusthree.stage.nature-particle
  (:require
   [clojure.spec.alpha :as s]
   [fastmath.vector :as v]
   [minusthree.engine.macros :refer [insert! s->]]
   [minusthree.engine.time :as time]
   [minusthree.engine.transform2d :as t2d]
   [minusthree.engine.types :as types]
   [minusthree.engine.world :as world :refer [esse]]
   [minusthree.rendering.atlas :as atlas]
   [odoyle.rules :as o]))

;; hardcoded 1016 is the value from the size of atlas texture
(def stage-width 1016)
(def stage-height 1016)

(defn init-fn [world _game]
  (-> world
      (o/insert ::player ::t2d/position (v/vec2 0.0 0.0))))

(def trees
  ["foliagePack_006.png"
   "foliagePack_007.png"
   "foliagePack_008.png"
   "foliagePack_009.png"
   "foliagePack_010.png"])

(def bushes
  ["foliagePack_049.png"
   "foliagePack_050.png"
   "foliagePack_051.png"
   "foliagePack_052.png"])

(def rocks
  ["foliagePack_055.png"
   "foliagePack_061.png"])

(def grass
  ["foliagePack_018.png"
   "foliagePack_019.png"
   "foliagePack_020.png"
   "foliagePack_021.png"])

(def terrains
  ["foliagePack_leaves_002.png"
   "foliagePack_leaves_010.png"])

(defn after-refresh [world _game]
  (let [base-speed -1]
    (-> world
        (esse ::cloud
              (atlas/foliage-instance
               {:tex-name "foliagePack_050.png"
                :scale (v/vec2 2.0 2.0)})
              {::atlas/layer -3
               ::atlas/tint [1.0 1.0 1.0 1.0]
               ::t2d/position (v/vec2 0.0 0.0)})
        (esse ::something
              (atlas/foliage-instance
               {:tex-name "foliagePack_003.png"
                :scale (v/vec2 2.0 2.0)})
              {::atlas/layer -3
               ::atlas/tint [1.0 1.0 1.0 0.7]
               ::t2d/position (v/vec2 0.0 120.0)})

        #_(esse ::debug
                (atlas/foliage-instance
                 {:tex-name "foliagePack_022.png"
                  :scale (v/vec2 2.0 2.0)})
                {::atlas/layer -3
                 ::atlas/tint [1.0 1.0 1.0 0.0]
                 ::t2d/position (v/vec2 0.0 620.0)})

        #_(esse ::ground-particle
                {::particle-config
                 {:tex-fn (fn [] "foliagePack_leaves_010.png")
                  :scale (v/vec2 18.0 3.0)
                  :starting-pos (v/vec2 2400.0 -700.0)
                  :layer 1
                  :tint [1.0 1.0 1.0 0.6]
                  :delay-time-fn (fn [] (/ 2000 (abs base-speed)))
                  :parallax-x-rate base-speed}
                 ::acc-time 0})


        ;; this *-init is workaround for now
        (esse ::far-grass-particle-init
              (atlas/foliage-instance
               {:tex-name "foliagePack_leaves_002.png"
                :scale (v/vec2 12.0 3.0)})
              {::atlas/layer 0
               ::atlas/tint [0.8 0.9 0.8 0.8]
               ::t2d/position (v/vec2 -300 -600.0)
               ::parallax-x-rate (* 0.25 base-speed)
               ::particle? true})

        (esse ::far-grass-particle-init-1
              (atlas/foliage-instance
               {:tex-name "foliagePack_leaves_002.png"
                :scale (v/vec2 9.0 3.0)})
              {::atlas/layer 0
               ::atlas/tint [0.8 0.9 0.8 0.8]
               ::t2d/position (v/vec2 900 -600.0)
               ::parallax-x-rate (* 0.25 base-speed)
               ::particle? true})

        (esse ::far-grass-particle-init-2
              (atlas/foliage-instance
               {:tex-name "foliagePack_leaves_002.png"
                :scale (v/vec2 9.0 3.0)})
              {::atlas/layer 0
               ::atlas/tint [0.8 0.9 0.8 0.8]
               ::t2d/position (v/vec2 1500 -600.0)
               ::parallax-x-rate (* 0.25 base-speed)
               ::particle? true})

        (esse ::far-grass-particle
              {::particle-config
               (let [further (* 0.25 base-speed)]
                 {:tex-fn (fn [] "foliagePack_leaves_002.png")
                  :scale (v/vec2 9.0 3.0)
                  :starting-pos (v/vec2 2400.0 -600.0)
                  :layer 0
                  :tint [0.8 0.9 0.8 0.8]
                  :delay-time-fn (fn [] (/ 900 (abs further)))
                  :parallax-x-rate further})
               ::acc-time 0})


        (esse ::grass-particle-init
              (atlas/foliage-instance
               {:tex-name "foliagePack_leaves_002.png"
                :scale (v/vec2 9.0 3.0)})
              {::atlas/layer 2
               ::atlas/tint [0.6 0.7 0.6 0.5]
               ::t2d/position (v/vec2 -900 -900.0)
               ::parallax-x-rate base-speed
               ::particle? true})

        (esse ::grass-particle-init-2
              (atlas/foliage-instance
               {:tex-name "foliagePack_leaves_002.png"
                :scale (v/vec2 9.0 3.0)})
              {::atlas/layer 2
               ::atlas/tint [0.6 0.7 0.6 0.5]
               ::t2d/position (v/vec2 0 -900.0)
               ::parallax-x-rate base-speed
               ::particle? true})

        (esse ::grass-particle-init-3
              (atlas/foliage-instance
               {:tex-name "foliagePack_leaves_002.png"
                :scale (v/vec2 9.0 3.0)})
              {::atlas/layer 2
               ::atlas/tint [0.6 0.7 0.6 0.5]
               ::t2d/position (v/vec2 900 -900.0)
               ::parallax-x-rate base-speed
               ::particle? true})

        (esse ::grass-particle-init-4
              (atlas/foliage-instance
               {:tex-name "foliagePack_leaves_002.png"
                :scale (v/vec2 9.0 3.0)})
              {::atlas/layer 2
               ::atlas/tint [0.6 0.7 0.6 0.5]
               ::t2d/position (v/vec2 1800 -900.0)
               ::parallax-x-rate base-speed
               ::particle? true})

        (esse ::grass-particle
              {::particle-config
               {:tex-fn (fn [] "foliagePack_leaves_002.png")
                :scale (v/vec2 9.0 3.0)
                :starting-pos (v/vec2 2400.0 -900.0)
                :layer 2
                :tint [0.6 0.7 0.6 0.5]
                :delay-time-fn (fn [] (/ 900 (abs base-speed)))
                :parallax-x-rate base-speed}
               ::acc-time 0}))))

(s/def ::offset-pos ::types/vec2)

(s/def ::scale ::types/vec2)
(s/def ::layer int?)
(s/def ::tint vector?)
(s/def ::starting-pos ::types/vec2)
(s/def ::tex-fn fn?)
(s/def ::delay-time-fn fn?)
(s/def ::parallax-x-rate number?)
(s/def ::particle-config
  (s/keys :req-un [::tex-fn ::scale ::layer ::tint ::starting-pos ::delay-time-fn ::parallax-x-rate]))
(s/def ::acc-time number?)
(s/def ::particle? boolean?)

(def rules
  (o/ruleset
   {::nature-as-particle
    [:what
     [::time/now ::time/delta dt]
     [particle-source-id ::particle-config config]
     [particle-source-id ::acc-time acc-time {:then false}]
     :then
     (if (< acc-time 0)
       (let [{:keys [tex-fn scale layer tint starting-pos parallax-x-rate]} config]
         (s-> session
              (esse (str particle-source-id (random-uuid))
                    (atlas/foliage-instance
                     {:tex-name (tex-fn)
                      :scale scale})
                    {::atlas/layer layer
                     ::atlas/tint tint
                     ::t2d/position starting-pos
                     ::parallax-x-rate parallax-x-rate
                     ::particle? true})
              (o/insert particle-source-id ::acc-time ((:delay-time-fn config)))))
       (insert! particle-source-id ::acc-time (- acc-time dt)))]

    ::nature-parallax
    [:what
     [::time/now ::time/delta dt]
     [particle-id ::parallax-x-rate parallax-x-rate]
     [particle-id ::t2d/position pos {:then false}]
     :then
     (let [move (v/vec2 (* dt parallax-x-rate) 0.0)]
       (insert! particle-id ::t2d/position (v/add pos move)))]

    ::live-particle
    [:what
     [particle-id ::t2d/position pos]
     [particle-id ::particle? true {:then false}]
     :then
     (when (< (.x pos) -2200)
       (insert! particle-id ::particle? false))]

    #_#_::count-particle
      [:what
       [particle-id ::particle? state]
       :then-finally
       (println "live particle: " (count (o/query-all session ::count-particle)))]

    ::delete-particle
    [:what
     [particle-id ::particle? false]
     [particle-id attr _]
     :then
     (s-> session (o/retract particle-id attr))]}))

(def system
  {::world/init-fn #'init-fn
   ::world/after-refresh #'after-refresh
   ::world/rules #'rules})

(comment
  (let [rng (java.util.Random. 42)]
    (repeatedly 10 #(.nextFloat rng)))

  :-)
