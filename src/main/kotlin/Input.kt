package com.camerontauxe.futz

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.MouseButton
import javafx.event.EventHandler

private val NUM_KEY_CODES       = KeyCode.values().size
private val NUM_MOUSE_BUTTONS   = MouseButton.values().size

/**
 * Manages keyboard and mouse input for FUTZ.
 *
 * Each frame, the Input object maintains a list of all the keys and mouse buttons
 * currently being held down (represented by the JavaFX KeyCode and MouseButton enums) as well
 * as a list of keys and mouse buttons that were pressed or released during the previous frame.
 * These lists can be queried to control action in FUTZ.
 * There is also a value for the position of the mouse cursor, represented both in world-space
 * and relative to the viewport.
 *
 * Inputs made during a frame will not be represented until the next frame begins. This is so that
 * the state of all inputs can be gaurenteed to be constant throughout the duration of a frame.
 */
public object Input {

    // Private/Internal properties

    // Lists for keys
    // keysCurrentlyPressed is an array of booleans for each KeyCode
    // (indexed by the KeyCode ordinal value)
    private val keysCurrentlyPressed    = BooleanArray(NUM_KEY_CODES)
    // Buffered changes to apply next frame (when update is called)
    private val keysPressedQueue        = mutableListOf<KeyCode>()
    private val keysReleasedQueue       = mutableListOf<KeyCode>()
    // List of keys pressed or released during the previous frame
    private val keysJustPressed         = mutableListOf<KeyCode>()
    private val keysJustReleased        = mutableListOf<KeyCode>()

    // Lists for mouse buttons
    // mouseCurrentlyPressed is an array of booleans for each MouseButton
    // (indexed by the MouseButton ordinal value)
    private val mouseCurrentlyPressed   = BooleanArray(NUM_MOUSE_BUTTONS)
    // Buffered changes to apply next frame (when update is called)
    private val mousePressedQueue       = mutableListOf<MouseButton>()
    private val mouseReleasedQueue      = mutableListOf<MouseButton>()
    // List of mouse buttons pressed or released during the previous frame
    private val mouseJustPressed        = mutableListOf<MouseButton>()
    private val mouseJustReleased       = mutableListOf<MouseButton>()

    // Buffered position of mouse cursor (applied to mousePosition when update is called)
    private var mouseViewportPositionBuffer =   Vector2(0.0, 0.0)
    // Whether or not the mouse was moved during this frame
    private var mouseMoved: Boolean = false

    // Public properties

    /**
     * The current position of the mouse (in pixels) relative to the upper-left-hand
     * corner of the viewport. (read-only)
     */
    var mouseViewportPosition   = Vector2(0.0, 0.0)
        private set
    /**
     * The current position of the mouse (in game-units) relative to the world-origin (read-only)
     */
    var mousePosition           = Vector2(0.0, 0.0)
        private set

    // Private/Internal functions

    /**
     * Initialize input by adding listeners to the JavaFX scene and Canvas
     */
    internal fun init() {
        FUTZ.fxScene.addEventHandler(KeyEvent.KEY_PRESSED,
            (object : EventHandler<KeyEvent> {
                override fun handle(keyEvent: KeyEvent) {
                    if (!keysPressedQueue.contains(keyEvent.code))
                        keysPressedQueue.add(keyEvent.code)
                }
            })
        );
        FUTZ.fxScene.addEventHandler(KeyEvent.KEY_RELEASED,
            (object : EventHandler<KeyEvent> {
                override fun handle(keyEvent: KeyEvent) {
                    if (!keysReleasedQueue.contains(keyEvent.code))
                        keysReleasedQueue.add(keyEvent.getCode())
                }
            })
        );
        FUTZ.canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
            (object : EventHandler<MouseEvent> {
                override fun handle(mouseEvent: MouseEvent) {
                    if (!mousePressedQueue.contains(mouseEvent.button))
                        mousePressedQueue.add(mouseEvent.button)
                }
            })
        );
        FUTZ.canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
            (object : EventHandler<MouseEvent> {
                override fun handle(mouseEvent: MouseEvent) {
                    if (!mouseReleasedQueue.contains(mouseEvent.button))
                        mouseReleasedQueue.add(mouseEvent.button)
                }
            })
        );

        val mouseMoveHandler = (object : EventHandler<MouseEvent> {
            override fun handle(mouseEvent: MouseEvent) {
                mouseViewportPositionBuffer = Vector2(mouseEvent.x,mouseEvent.y)
                mouseMoved = true
            }
        })
        FUTZ.canvas.addEventHandler(MouseEvent.MOUSE_MOVED,mouseMoveHandler)
        FUTZ.canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,mouseMoveHandler)
    }

    /**
     * Update the state of all inputs to account changes made during the previous frame.
     */
    internal fun update() {
        // Add keys that were pressed
        keysJustPressed.clear()
        for (code in keysPressedQueue) {
            keysJustPressed.add(code)
            keysCurrentlyPressed[code.ordinal] = true
        }
        keysPressedQueue.clear()

        // Add keys that were released
        keysJustReleased.clear()
        for (code in keysReleasedQueue) {
            keysJustReleased.add(code)
            keysCurrentlyPressed[code.ordinal] = false
        }
        keysReleasedQueue.clear()

        // Add mouse buttons that were pressed
        mouseJustPressed.clear()
        for (button in mousePressedQueue) {
            mouseJustPressed.add(button)
            mouseCurrentlyPressed[button.ordinal] = true
        }
        mousePressedQueue.clear()

        // Add mouse buttons that were released
        mouseJustReleased.clear()
        for (button in mouseReleasedQueue) {
            mouseJustReleased.add(button)
            mouseCurrentlyPressed[button.ordinal] = false
        }
        mouseReleasedQueue.clear()

        // Update mouse position if it was moved.
        if (mouseMoved) {
            mouseViewportPosition   = mouseViewportPositionBuffer.copy()
            // @TODO: Mouse world position will not update when the camera moves. Fix that
            mousePosition           = Viewport.viewportPosToWorldPos(mouseViewportPosition)
            mouseMoved = false
        }
    }

    // Public functions

    /** 
     * Check if the key represented by the given JavaFX KeyCode is currently being held down.
     *
     * Note: if the key was pressed AND released all during the previous frame, it will appear
     * here as not being pressed, but will still be visible in [wasJustPressed] and
     * [wasJustReleased]
     */
    public fun isPressed(code: KeyCode):        Boolean = keysCurrentlyPressed[code.ordinal]
    /** 
     * Check if the mouse button represented by the given JavaFX
     * MouseButton is currently being held down.
     *
     * Note: if the button was pressed AND released all during the previous frame, it will appear
     * here as not being pressed, but will still be visible in [wasJustPressed] and
     * [wasJustReleased]
     */
    public fun isPressed(button: MouseButton):  Boolean = mouseCurrentlyPressed[button.ordinal]

    /**
     * Check if the key represented by the given JavaFX KeyCode was just pressed in the previous
     * frame.
     */
    public fun wasJustPressed(code: KeyCode):       Boolean = code in keysJustPressed
    /**
     * Check if the mouse button represented by the given JavaFX
     * MouseButton was just pressed in the previous
     * frame.
     */
    public fun wasJustPressed(button: MouseButton): Boolean = button in mouseJustPressed

    /**
     * Check if the key represented by the given JavaFX KeyCode was just released in the previous
     * frame.
     */
    public fun wasJustReleased(code: KeyCode):          Boolean = code in keysJustReleased
    /**
     * Check if the mouse button represented by the given JavaFX
     * MouseButton was just released in the previous
     * frame.
     */
    public fun wasJustReleased(button: MouseButton):    Boolean = button in mouseJustReleased
}