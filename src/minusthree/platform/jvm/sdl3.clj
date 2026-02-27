(ns minusthree.platform.jvm.sdl3
  (:require
   [minusthree.engine.engine :as engine]
   [minusthree.engine.time :as time]
   [minusthree.platform.jvm.sdl3-events :as sdl3-input])
  (:import
   [org.lwjgl.opengl GL]
   [org.lwjgl.sdl
    SDLError
    SDLInit
    SDLTimer
    SDLVideo
    SDL_Event]))

(defn throw-sdl-error [msg]
  (throw (ex-info msg {:data (SDLError/SDL_GetError)})))

(defn create-window
  ([{:keys [w h title]
     :or   {w 1280 h 720 title "Hello, dofida-3 + sdl3!"}}]
   ;; gl ver
   (SDLVideo/SDL_GL_SetAttribute SDLVideo/SDL_GL_CONTEXT_FLAGS SDLVideo/SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG)
   (SDLVideo/SDL_GL_SetAttribute SDLVideo/SDL_GL_CONTEXT_PROFILE_MASK SDLVideo/SDL_GL_CONTEXT_PROFILE_CORE)
   (SDLVideo/SDL_GL_SetAttribute SDLVideo/SDL_GL_CONTEXT_MAJOR_VERSION 4)
   (SDLVideo/SDL_GL_SetAttribute SDLVideo/SDL_GL_CONTEXT_MINOR_VERSION 5)

   ;; graphics context
   (SDLVideo/SDL_GL_SetAttribute SDLVideo/SDL_GL_DOUBLEBUFFER 1)
   (SDLVideo/SDL_GL_SetAttribute SDLVideo/SDL_GL_DEPTH_SIZE 24)
   (SDLVideo/SDL_GL_SetAttribute SDLVideo/SDL_GL_STENCIL_SIZE 0)
   (let [window-flags (bit-or SDLVideo/SDL_WINDOW_OPENGL SDLVideo/SDL_WINDOW_RESIZABLE)
         width        (int w)
         height       (int h)
         sdl-window   (SDLVideo/SDL_CreateWindow title width height window-flags)]
     (when (= sdl-window 0) (throw-sdl-error "SDLVideo/SDL_CreateWindow error!"))
     (SDLVideo/SDL_GL_SetSwapInterval 1) ;; enable vsync
     (SDLVideo/SDL_SetWindowPosition sdl-window SDLVideo/SDL_WINDOWPOS_CENTERED SDLVideo/SDL_WINDOWPOS_CENTERED)
     sdl-window)))

(defn create-gl-context [sdl-window]
  (let [gl-context (SDLVideo/SDL_GL_CreateContext sdl-window)]
    (when (= gl-context 0) (throw-sdl-error "SDLVideo/SDL_GL_CreateContext error!"))
    (SDLVideo/SDL_GL_MakeCurrent sdl-window gl-context)
    (GL/createCapabilities)
    gl-context))

(defn start-sdl3-loop
  [sdl-window {:keys [stop-flag* :refresh-flag*] :as config}]
  (println "hello -3 + sdl")
  (when-not (SDLInit/SDL_Init SDLInit/SDL_INIT_VIDEO)
    (throw-sdl-error "SDLInit/SDL_Init error!"))
  (let [gl-context (create-gl-context sdl-window)
        stop-flag* (or stop-flag* (atom false))
        event-buf  (SDL_Event/create)]
    (letfn [(we-begin-the-game []
              (SDLVideo/SDL_ShowWindow sdl-window)
              {::time/total   0.0
               :config        (dissoc config :stop-flag*)
               :sdl-window    sdl-window
               :refresh-flag* refresh-flag*})

            (do-we-stop? [{::sdl3-input/keys [stop?]}]
              (or stop? (and (some? stop-flag*) @stop-flag*)))

            (do-we-refresh? [_game]
              (let [refresh? (some-> refresh-flag* deref)]
                (when refresh? (reset! refresh-flag* false))
                refresh?))

            (things-from-out-there [game]
              (let [total (SDLTimer/SDL_GetTicks)
                    delta (- total (::time/total game))
                    game  (time/update-time game total delta)
                    game' (sdl3-input/poll-events game event-buf)]
                (SDLVideo/SDL_GL_SwapWindow sdl-window)
                game'))

            (the-game-ends []
              (println "the game ends")
              (SDLVideo/SDL_GL_DestroyContext gl-context)
              (SDLVideo/SDL_DestroyWindow sdl-window)
              (SDLInit/SDL_Quit))]
      (engine/game-loop
       #::engine{:we-begin-the-game     we-begin-the-game
                 :do-we-stop?           do-we-stop?
                 :do-we-refresh?        do-we-refresh?
                 :things-from-out-there things-from-out-there
                 :the-game-ends         the-game-ends}))))
