package com.camerontauxe.futz.prefab

import com.camerontauxe.futz.*

import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.text.Font

/**
 * The scene used for the Splash Screen displayed when a FUTZ game starts.
 *
 * The Splash Screen gives time for a user-defined loading function to complete. Once that
 * is done, the splash screen automatically moves on to the next scene after either the user
 * pressed space or some amount of time has elapsed (whichever happens first)
 */
internal object SplashScreen : Scene() {
    override val name = "Splash Screen"

    /** The scene to be switched to after the splash screen finishes */
    internal lateinit var afterScene:   Scene
    /** Callback function used for loading */
    internal lateinit var loadCallback: () -> Boolean

    /** 
     * Whether the user has requested the Splash Screen to end (by pressing Space).
     * This will exit the splash screen immediately if loading has finished or otherwise
     * wait until loading has finished and exit the splash screen immediately after.
     */
    internal var skipRequested:  Boolean = false
    /**
     * Whether the loading callback has finished (returned true)
     */
    internal var loadDone:       Boolean = false

    /** Sprite with the "Powered By FUTZ" logo */
    private lateinit var logoSprite:    Sprite
    private var spriteLoaded:           Boolean = false

    private var loadingFont = Font("Liberation Sans", 0.35)

    /** Construct scene (load assets if necessary, spawn logo sprite and loading text) */
    override fun construct() {
        if (!spriteLoaded) {
            logoSprite      = Assets.loadSprite(path = "FUTZ/poweredBy.png", pixelsPerUnit = 175.0)
            spriteLoaded    = true
        }
        if (!Assets.liberationFontsLoaded) {
            Assets.loadLiberationFonts()
            loadingFont = Font("Liberation Sans", 0.35)
        }

        val logo = SpriteEntity(logoSprite)
        logo.centerInside(Viewport.worldRect)

        val loadingText = (object : TextEntity("Loading...", loadingFont) {
            override fun update() { if (loadDone) destroy() }
        });
        // Place text in lower-right-hand corner
        loadingText.position = MutableVector2(
            x = Viewport.worldRectOrigin.x2 - 2.0,
            y = Viewport.worldRectOrigin.y2 - 0.35
        )

        Entities.addAll(logo, loadingText)
    }

    /**
     * Each frame, call the loading callback if it hasn't finished yet
     * and exit the splash screen when it is time to do so.
     */
    override fun update() {
        if (Input.isPressed(KeyCode.SPACE))
            skipRequested = true

        if (!loadDone) {
            loadDone = loadCallback()
            if (loadDone)
                Debug.log("Loading callback finished. Splash screen will end soon...")
        }

        if (loadDone && (FUTZ.sceneTime > 2500 || skipRequested)) {
            FUTZ.setScene(afterScene)
        }
    }
}