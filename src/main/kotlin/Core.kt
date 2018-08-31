package com.camerontauxe.futz

import com.camerontauxe.futz.prefab.SplashScreen

import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.layout.Pane
import javafx.scene.input.KeyCode
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
// Don't import javafx.scene.Scene and instead use its fully-qualified name in the code
// to prevent clashing with a FUTZ scene (com.camerontauxe.futz.Scene)

// Default values for various attributes used if the user does not
// override them when calling FUTZ.init()
private const val DEFAULT_WINDOW_WIDTH: Double = 640.0
private const val DEFAULT_WINDOW_HEIGHT: Double = 360.0
private const val DEFAULT_TITLE: String = "My Futz Game"

/**
 * Represents the core of the FUTZ engine.
 *
 * This is a singleton object that manages all other pieces of the engine including the other
 * singletons ([Entities],[Assets],[Debug],etc.). Contains functions and properties pertaining
 * to the global state of FUTZ.
 *
 * The most important function here is [FUTZ.init] which starts the engine. Many other functions
 * may fail or simply do nothing if called before FUTZ has been initialized.
 *
 * @see FUTZ.init
 *
 * @author Cameron Tauxe
 */
public object FUTZ {

    /** Whether or not FUTZ has been initialized and is currently running. (Read-only) */
    public var isRunning: Boolean = false 
        private set

    // private and internal properties

    // Scene management
    /** The currently active Scene */
    private var currentScene:       Scene   = SplashScreen  // The first scene will
                                                            // always be the splash screen
    /** The scene to switch to at the end of this frame if [shouldChangeScene] is true */
    private var nextScene:          Scene   = EmptyScene
    /** Whether or not the scene should change to [nextScene] at the end of the current frame */
    private var shouldChangeScene:  Boolean = false

    // JavaFX components
    // (These will be initialized when FUTZ starts)
    internal lateinit var canvas:   Canvas
    internal lateinit var fxScene:  javafx.scene.Scene
    internal lateinit var stage:    Stage
    /**
     * The JavaFX graphics context used for rendering
     */
    public val graphicsContext:   GraphicsContext get() = canvas.graphicsContext2D //Shortcut

    /** 
     * This function will be called after the Splash Screen finishes.
     * This allows the user to interact with FUTZ (create entities,
     * load assets, change scenes, etc.) without having to explcitly
     * create a Scene to do it.
     *
     * This can be set by the user via an argument to [init]. If it's not
     * overridden, then it simply does nothing and the user will instead have
     * to create a custom initial scene to interact with FUTZ.
     */
    private var onStartCallback: () -> Unit = {}

    // Timing (Public, Read-Only values)
    /** The time (in seconds) that the previous frame took to execute. (Read-only) */
    public val frameTime:   Double  get() = Timer.frameTime
    /** The time (in seconds) that the game has been running in total. (Read-only) */
    public val gameTime:    Long    get() = Timer.gameTime
    /** The number of current frame. (Read-only) */
    public val frame:       Long    get() = Timer.frame
    /** The time (in seconds) since the last time the active Scene changed. (Read-only) */
    public val sceneTime:   Long    get() = Timer.sceneTime
    /** The number of the frame since the last time the active Scene changed. (Read-only) */
    public val sceneFrame:  Long    get() = Timer.sceneFrame

    // Publicly readable and settable values

    /**
     * The title of the game. Displayed as the title of the window.
     */
    public var title: String = DEFAULT_TITLE
        set(value) {
            // This can be set any time, but if it's set while the game is already running,
            // then we need to update the stage title
            if (isRunning)
                stage.title = value
            field = value
        }
    
    /**
     * A JavaFX Paint (Solid color, gradient, etc.) Used as the background of the game
     *
     * Most games will probably want to just cover it up with a sprite as the background,
     * though it is visible on the splash screen.
     *
     * @TODO Maybe it shouldn't be? If the user sets this before initializing the game,
     * then the splash screen will look different. But I probably don't want the user to
     * alter the look of the splash screen.
     */
    public var backgroundColor: Paint = Color.web("#EFE9C7")

    /**
     * Start the FUTZ engine.
     *
     * This function intializes JavaFX builds a stage and scene, initializes the input manager
     * and viewport and starts the main game loop. The parameters to this function control various
     * aspects of the game. Each parameter has a default value so it is not necessary to supply
     * values for every one of them. However, the user must supply either a function for
     * [startCallback] or a custom Scene for [initialScene] if they wish to be able to interact
     * with FUTZ. This is because [init] will NOT return until the game window is closed.
     *
     * @TODO Give a warning if neither initialScene or startCallback is overridden? It seems
     * like something that would be a common rookie mistake so it would be good to explain to 
     * first-time users why they called FUTZ.init and can't do anything
     *
     * @param [initialScene] The Scene to start the game in. This scene will be switched to
     * once the splash screen has finished.
     * @param [width] The width (in pixels) of the game window. While it is possible to resize the
     * window after FUTZ starts, the value given here along with [height] will be used to determine
     * the aspect ratio of the game which is fixed for the entire time the game is running.
     * @param [width] The height (in pixels) of the game window. While it is possible to resize the
     * window after FUTZ starts, the value given here along with [width] will be used to determine
     * the aspect ratio of the game which is fixed for the entire time the game is running.
     * @param [title] The title of the game displayed as the title of the window. This can also be
     * changed at any time through the [FUTZ.title] property.
     * @param [startCallback] Function to be called after the splash screen finishes and the first
     * scene has been constructed. This allows the user to interact with FUTZ after starting it
     * without having to define a custom scene.
     * @param [loadCallback] A function to be called during the splash screen to optionally
     * handle loading of assets or any other preparation needed before the game starts. To prevent
     * completely locking up the game while loading occurs this function is meant to be called
     * iteratively, doing a small amount of work each time and returning a boolean value
     * indicating whether or not it has finished. After returning 'true' the function won't be
     * called anymore and the splash screen will be allowed to end.
     */
    public fun init(
        initialScene:   Scene   = EmptyScene,
        width:          Double  = DEFAULT_WINDOW_WIDTH,
        height:         Double  = DEFAULT_WINDOW_HEIGHT,
        title:          String  = DEFAULT_TITLE,
        startCallback:  () -> Unit      = {},
        loadCallback:   () -> Boolean   = {true}
    ) {
        if (isRunning)
            throw IllegalStateException("FUTZ cannot be initialized twice!")

        // Set Splash Screen properties
        SplashScreen.afterScene     = initialScene
        SplashScreen.loadCallback   = loadCallback

        this.onStartCallback        = startCallback
        this.title = title

        // Initialize the Viewport with the given width and height
        // These values are used to calculate [Viewport.aspectRatio] which
        // is constant from this point on
        Viewport.initInWindow(windowWidth = width, windowHeight = height)

        // Load liberation fonts
        Fonts.loadLiberationFonts()

        // Start a JavaFX application
        Application.launch(FutzApp::class.java)
    }

    /**
     * Equivalent to JavaFX's application 'start' function. This is called on the JavaFX
     * application thread with the stage to use as the argument. Finishes initializing
     * FUTZ and starts the main game loop.
     */
    internal fun FXStart(stage: Stage) {
        this.stage = stage
        stage.title = title

        // Create canvas initially with the default Viewport size
        canvas = Canvas(Viewport.width, Viewport.height)

        // Add canvas to the JavaFX Scene (wrapped in a Pane) and add the
        // scene to the stage.
        val pane = Pane()
        pane.getChildren().add(canvas)
        fxScene = javafx.scene.Scene(pane)
        fxScene.fill = Color.BLACK
        stage.sizeToScene()
        stage.setScene(fxScene)

        // Handler for resizing the game window.
        // Recalculates the viewport and adjusts the canvas to fit.
        val stageResize = (object : ChangeListener<Number> {
            override fun changed(value: ObservableValue<out Number>, old: Number, new: Number) {
                Viewport.updateWindowSize(fxScene.width, fxScene.height)
                canvas.layoutX = Viewport.windowRect.x
                canvas.layoutY = Viewport.windowRect.y
                canvas.width = Viewport.width
                canvas.height = Viewport.height
            }
        });
        stage.widthProperty().addListener(stageResize);
        stage.heightProperty().addListener(stageResize);

        // Initializiing Input adds key and mouse listeners to the JavaFX scene
        // to be managed by the Input object
        Input.init()

        isRunning = true

        currentScene.construct()
        // Open window
        stage.show()

        // Start game timer. FUTZ.doFrame() will now be called every frame. (60 times a second)
        Timer.start()

        Debug.log("FUTZ Successfully Intitialized!!!")
    }

    /**
     * Called 60 times a second by the game timer. Performs one frame of action for the game
     */
    internal fun doFrame() {

        // Clear the frame with background color
        val ctx = graphicsContext
        ctx.fill = backgroundColor
        ctx.fillRect(0.0, 0.0, Viewport.width, Viewport.height)

        // Update Input, so that it reflects the inputs made
        // over the course of the previous frame
        Input.update()

        // Update all entities (including the custom update routine for the scene)
        // This is skipped if the debugger has the game paused
        if (!Debug.gameHalted) {
            currentScene.update()
            Entities.update()
        }

        // Render all entities (including the custom render routine for the scene)
        Entities.render()
        currentScene.render()

        // If the F1 was pressed, toggle debug mode
        if (Input.wasJustReleased(KeyCode.F1)) {
            Debug.enabled = !Debug.enabled
            // If we're now closing debug mode, resume the game if it was paused
            // and reset the camera if it was in freeform mode
            if (!Debug.enabled) {
                Debug.gameHalted = false
                if (Debug.freeformCamera) {
                    Debug.freeformCamera = false
                    Camera.position = Debug.savedCameraPos.mutableCopy()
                    Camera.zoom = Debug.savedCameraZoom
                }
            }
        }
        // Pressing F2 also toggle the debug pause function specifically
        if (Input.wasJustReleased(KeyCode.F2)) {
            Debug.gameHalted = !Debug.gameHalted
            // If the debugger is closed when pressing F2, open it
            // (will immediately be in pause mode)
            if (Debug.gameHalted)
                Debug.enabled = true
        }

        if (Debug.enabled)
            Debug.update()

        // If we need to change the Scene, destroy the current Scene,
        // remove all entities and construct the new Scene
        if (shouldChangeScene) {
            Debug.log("Changing to scene: ${nextScene.name}")

            val fromSplashScreen = (currentScene == SplashScreen)

            currentScene.destroy()
            currentScene = nextScene

            Entities.clearAll()

            shouldChangeScene = false
            Timer.resetSceneTime()
            nextScene.construct()

            // If changing from the splash screen, we're now in the first
            // actual game scene so call the start callback function
            if (fromSplashScreen) onStartCallback()
        }
    }

    /**
     * Change FUTZ to the given Scene. Note that the scene will not
     * actually be changed until the end of the frame
     *
     * @TODO: If this is called before [init], then it can be used to
     * skip the splash screen. Should that be allowed?
     */
    public fun setScene(newScene: Scene) {
        if (!isRunning) { currentScene = newScene; return }
        nextScene = newScene
        shouldChangeScene = true
    }
}

/**
 * A wrapper class for starting a JavaFX application
 */
internal class FutzApp : Application() {
    override fun start(stage: Stage) {
        FUTZ.FXStart(stage);
    }
}
