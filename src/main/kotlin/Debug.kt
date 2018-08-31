package com.camerontauxe.futz

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Paint
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton

// Constants for sizing and layout of parts of the Debug UI
private const val BIG_TEXT_SIZE:        Double = 16.0
private const val SMALL_TEXT_SIZE:      Double = 12.0
private const val TEXT_BOX_PADDING:     Double = 6.0
private const val BIG_TEXT_BOX_SIZE:    Double = BIG_TEXT_SIZE      + TEXT_BOX_PADDING * 2.0
private const val SMALL_TEXT_BOX_SIZE:  Double = SMALL_TEXT_SIZE    + TEXT_BOX_PADDING * 2.0
private const val BUTTON_SIZE:          Double = 35.0
private const val BUTTON_MARGIN:        Double = 5.0

// Constants for Debug Console
private const val MAX_MESSAGES:         Int = 50
private const val MESSAGES_PER_PAGE:    Int = 5

// Constants for camera movement
private const val CAMERA_SPEED:         Double = 2.5
private const val CAMERA_ZOOM_SPEED:    Double = 2.5

// Fonts used in debug UI. (These may get reloaded if Liberation fonts haven't been loaded yet)
private var bigText:    Font = Font("Liberation Mono", BIG_TEXT_SIZE)
private var smallText:  Font = Font("Liberation Mono", SMALL_TEXT_SIZE)

// Colors in Debug UI
private val textColor:  Paint = Color.WHITE;
private val textBg:     Paint = Color.web("#000000c0") // transparent black
private val textBg2:    Paint = Color.web("#000000d0") // darker
private val textBg3:    Paint = Color.web("#000000d8") // darkest

// Sprites used in Debug UI. Loaded and initiailzed the first time debug is enabled
private var stopIcon:   Sprite = Sprite("FUTZ/debug_stop.png",          100.0, 100.0)
private var upIcon:     Sprite = Sprite("FUTZ/debug_scrollup.png",      100.0, 100.0)
private var downIcon:   Sprite = Sprite("FUTZ/debug_scrolldown.png",    100.0, 100.0)
private var cameraIcon: Sprite = Sprite("FUTZ/debug_camera.png",        100.0, 100.0)
private var hitboxIcon: Sprite = Sprite("FUTZ/debug_hitbox.png",        100.0, 100.0)
private var spritesLoaded: Boolean = false

/**
 * Manages the Debugging interface and console.
 *
 * In FUTZ, the Debug interface can be summoned by pressing F1. It provides a read-out of
 * various information, a console for displaying log and error messages and provides functions
 * such as force-pausing the game, manually moving the camera or displaying
 * the hitboxes on entitites.
 */
public object Debug {

    /**
     * Whether or not the debug interface is currently being displayed 
     * @TODO: Can this be made public?
     */
    internal var enabled:       Boolean = false;

    /** The messages currently in the console. (indexed with most recent at the end) */
    internal val messages               = mutableListOf<ConsoleMessage>()
    /** 
     * The amount that the console is "scrolled up" counted by number of messages.
     * '0' indicates that we are viewing the bottom (i.e. the most recent messages)
     */
    internal var messageScroll: Int     = 0

    /** Whether or not the game is paused. Entities will not update while this is 'true' */
    internal var gameHalted:        Boolean = false
    /** Whether or not the debug UI should display the hitboxes and names of entities */
    internal var showHitboxes:      Boolean = false
    /** Whether or not the user has control manual control of the camera */
    internal var freeformCamera:    Boolean = false

    // While the user is controlling the camera in freeform mode, we save the last
    // position and zoom level of the camera (just before entering freeform mode) mode
    // so that it can be restored upon exiting freeform mode
    internal var savedCameraPos     = Camera.position.copy()
    internal var savedCameraZoom    = Camera.zoom

    /**
     * Update and display the debug UI. (Note that, at this point, the graphicsContext)
     * is scaled to viewport (in pixels))
     */
    internal fun update() {

        // Load necessary assets if they haven't been loaded already
        if (!spritesLoaded)
            loadSprites()

        // Draw UI
        if (showHitboxes) {
            drawHitboxes()
        }
        drawConsole()
        drawInfo()
        drawButtons() // The buttons are immediate mode, so drawing them also updates their values
        drawRightSideMessages()

        // Update camera movement if in freeform mode
        if (freeformCamera) {
            updateCamera()
        }

    }

    /**
     * Display a log message in the console
     */
    public fun log(message: String) {
        messageScroll = 0
        messages.add(ConsoleMessage(message,MESSAGE_TYPE.NORMAL))
        if (messages.size > MAX_MESSAGES) messages.removeAt(0)
        println("[FUTZ] [LOG]     " + message)
    }
    /**
     * Display an error message in the console
     */
    public fun error(message: String) {
        messageScroll = 0
        messages.add(ConsoleMessage(message,MESSAGE_TYPE.ERROR))
        if (messages.size > MAX_MESSAGES) messages.removeAt(0)
        println("[FUTZ] [ERROR]   " + message)
    }
    /**
     * Display a warning message in the console
     */
    public fun warning(message: String) {
        messageScroll = 0
        messages.add(ConsoleMessage(message,MESSAGE_TYPE.WARNING))
        if (messages.size > MAX_MESSAGES) messages.removeAt(0)
        println("[FUTZ] [WARNING] " + message)
    }

    /** Draw the hitboxes and names of entities */
    private fun drawHitboxes() {
        val ctx = FUTZ.graphicsContext
        ctx.lineWidth = 2.0

        for (e in Entities.list.filter{it.visible}) {
            if (e.collidable) {
                // Hitboxes that are currently colliding are drawn in red, not green
                ctx.stroke = if (e.collidingEntities.isEmpty()) Color.LIMEGREEN else Color.CRIMSON
                // Draw hitbox
                ctx.strokeRect(e.hitbox.worldRect.worldSpaceToViewport())
            }
            // Draw a text box with the entities name
            drawTextBox(text = e.name, position = e.viewportPos)
        }
    }

    /** Draw the info in the upper-left corner of the debug UI */
    private fun drawInfo() {
        val cursor = MutableVector2(0.0,0.0)

        // "Press F1 to exit" message
        drawTextBox(text = "Press F1 to exit debug mode", position = cursor, boxFill = textBg3)
        cursor.y += SMALL_TEXT_BOX_SIZE

        // Framerate readout
        val fps = "FPS: %.1f".format(1.0 / FUTZ.frameTime)
        drawTextBox(text = fps, position = cursor, boxFill = textBg2)
        cursor.y += SMALL_TEXT_BOX_SIZE

        // Number of active entities
        val entities = "Entities: ${Entities.list.size}"
        drawTextBox(text = entities, position = cursor)
        cursor.y += SMALL_TEXT_BOX_SIZE

        // Camera position (drawn in red while in freeform mode)
        val cameraPos = "Camera Position (World): X: %.3f, Y: %.3f Zoom: %.2f%s".format(
            Camera.position.x, Camera.position.y, Camera.zoom, if (freeformCamera) " (Freeform mode)" else "")
        drawTextBox(
            text = cameraPos, position = cursor,
            textFill = if (freeformCamera) Color.RED else textColor,
            boxFill = textBg2
        )
        cursor.y += SMALL_TEXT_BOX_SIZE

        // Mouse viewport position readout
        val mouseViewport = "Mouse Position (Viewport): X: %.1f, Y: %.1f".format(
            Input.mouseViewportPosition.x, Input.mouseViewportPosition.y)
        drawTextBox(text = mouseViewport, position = cursor)
        cursor.y += SMALL_TEXT_BOX_SIZE

        // Mouse world position readout
        val mouseWorld = "Mouse Position (World): X: %.3f, Y: %.3f".format(
            Input.mousePosition.x, Input.mousePosition.y)
        drawTextBox(text = mouseWorld, position = cursor, boxFill = textBg2)
        cursor.y += SMALL_TEXT_BOX_SIZE

        // All mouse buttons being pressed
        var buttons = "Mouse Buttons: "
        for (button in MouseButton.values())
            if (Input.isPressed(button)) buttons += (button.name+" ")
        drawTextBox(text = buttons, position = cursor)
        cursor.y += SMALL_TEXT_BOX_SIZE

        // All keys being pressed
        var keys = "Keys: "
        for (key in KeyCode.values())
            if (Input.isPressed(key)) keys += (key.name+" ")
        drawTextBox(text = keys, position = cursor, boxFill = textBg2)
        cursor.y += SMALL_TEXT_BOX_SIZE
    }

    /** Draw the debug console */
    private fun drawConsole() {
        val ctx = FUTZ.graphicsContext
        val screen = Viewport.rect
        val cursor = MutableVector2(BUTTON_SIZE, screen.height - BUTTON_SIZE)

        // Number of messages to be drawn
        val messagesToDraw = Math.min(MESSAGES_PER_PAGE, messages.size)
        // The index of bottom (most recent, higher index) message to draw
        val base    = messages.size - 1 - messageScroll
        // The index of the top (oldest, lower index) message to draw
        val top     = Math.max(0, base - messagesToDraw + 1)
        // Draw messages
        for (i in base downTo top) {
            val message = messages.get(i)
            val fill = if (i % 2 == 0) textBg else textBg2 // alternate background
            cursor.y -= SMALL_TEXT_BOX_SIZE
            drawTextBox(
                text = message.text, position = cursor,
                textFill = message.type.fill, boxFill = fill
            )
        }

        // If there are more messages above those drawn (include a message saying how many)
        if (top > 0) {
            cursor.y -= SMALL_TEXT_BOX_SIZE
            drawTextBox(text = "+ ${top} more messages...", position = cursor, boxFill = textBg3)
        }

        // Draw scroll bar

        val scrollBarSize = SMALL_TEXT_BOX_SIZE * messagesToDraw //size of scroll bar minus the buttons
        cursor.x = 0.0

        //Draw "scroll up" button and scroll up if it is being pressed
        cursor.y = screen.height - scrollBarSize - (BUTTON_SIZE * 2.0)
        if (button(sprite = upIcon,position = cursor) && top > 0)
            messageScroll++

        // Draw middle of scroll bar
        cursor.y += BUTTON_SIZE
        ctx.setFill(textBg)
        ctx.fillRect(0.0,cursor.y,BUTTON_SIZE,scrollBarSize)

        // Draw "scroll down" button and scroll down if it is being pressed
        cursor.y += scrollBarSize
        if (button(sprite = downIcon, position = cursor) && messageScroll > 0)
            messageScroll--
    }

    /** Draw the buttons to toggle different modes in the upper-right corner of the UI */
    private fun drawButtons() {
        val screen = Viewport.rect
        val cursor = MutableVector2(screen.width - BUTTON_SIZE - BUTTON_MARGIN, BUTTON_MARGIN)

        // Draw the pause button and toggle pause if it is being pressed
        val toggleHalt: Boolean = button(
            sprite  = stopIcon, position = cursor,
            tooltip = "Pause Game (F2)", toggleOn = gameHalted
        )
        if (toggleHalt) gameHalted = !gameHalted

        cursor.x -= (BUTTON_MARGIN + BUTTON_SIZE)

        // Draw the freeform camera mode button and toggle if it is being pressed
        val toggleCamera: Boolean = button(
            sprite  = cameraIcon, position = cursor,
            tooltip = "Freeform Camera", toggleOn = freeformCamera
        )
        if (toggleCamera) {
            freeformCamera = !freeformCamera
            // If switching into freeform mode, save the camera position and zoom
            if (freeformCamera) {
                savedCameraPos  = Camera.position.copy()
                savedCameraZoom = Camera.zoom
                gameHalted      = true
            // If switching out of freeform mode, restore the camera position and zoom
            } else {
                Camera.position = savedCameraPos.mutableCopy()
                Camera.zoom     = savedCameraZoom
            }
        }

        cursor.x -= (BUTTON_MARGIN + BUTTON_SIZE)

        // Draw the "display hitboxes" button and toggle if it is being pressed
        val toggleHitboxes: Boolean = button(
            sprite  = hitboxIcon,   position = cursor,
            tooltip = "Show hitboxes", toggleOn = showHitboxes
        )
        if (toggleHitboxes) showHitboxes = !showHitboxes
    }

    /** Draw the messages below the buttons on the right-side of the screen */
    private fun drawRightSideMessages() {
        val ctx     = FUTZ.graphicsContext
        val cursor  = MutableVector2(Viewport.width, BUTTON_SIZE + BUTTON_MARGIN * 2)

        // If the game is paused, draw a message saying so
        // and also draw a red border around the whole screen
        if (gameHalted) {
            ctx.lineWidth = 5.0
            ctx.stroke = Color.CRIMSON
            ctx.strokeRect(Viewport.rect)
            drawTextBox(
                text = "Game is PAUSED", position = cursor,
                font = bigText, textFill = Color.RED, boxFill = textBg3,
                reverse = true)
            cursor.y += BIG_TEXT_BOX_SIZE
        }

        // If in freeform camera mode draw some instructions and
        // give a readout of the saved position
        if (freeformCamera) {
            drawTextBox(
                text = "W,A,S,D to move Camera. Q,E to Zoom.",
                position = cursor, boxFill = textBg3, reverse = true
            )
            cursor.y += SMALL_TEXT_BOX_SIZE

            val saved = "Saved Camera Position: X: %.3f, Y: %.3f Zoom: %.2f".format(
                savedCameraPos.x, savedCameraPos.y, savedCameraZoom
            )
            drawTextBox(
                text = saved, position = cursor,
                boxFill = textBg3, reverse = true
            )
        }
    }

    /** Move the camera according to keyboard input */
    private fun updateCamera() {
        val up      = Input.isPressed(KeyCode.W)
        val down    = Input.isPressed(KeyCode.S)
        val right   = Input.isPressed(KeyCode.D)
        val left    = Input.isPressed(KeyCode.A)
        val zoomIn  = Input.isPressed(KeyCode.E)
        val zoomOut = Input.isPressed(KeyCode.Q)

        if (up && !down)
            Camera.position.y -= CAMERA_SPEED / Camera.zoom * FUTZ.frameTime
        else if (down && !up)
            Camera.position.y += CAMERA_SPEED / Camera.zoom * FUTZ.frameTime

        if (left && !right)
            Camera.position.x -= CAMERA_SPEED / Camera.zoom * FUTZ.frameTime
        else if (right && !left)
            Camera.position.x += CAMERA_SPEED / Camera.zoom * FUTZ.frameTime

        if (zoomIn && !zoomOut)
            Camera.zoom *= 1 + (0.25 * CAMERA_ZOOM_SPEED * FUTZ.frameTime)
        else if (zoomOut && !zoomIn)
            Camera.zoom *= 1 - (0.2 * CAMERA_ZOOM_SPEED * FUTZ.frameTime)
    }

    /** Load all of the sprites needed for the debug UI */
    private fun loadSprites() {
        stopIcon.load()
        upIcon.load()
        downIcon.load()
        cameraIcon.load()
        hitboxIcon.load()
        spritesLoaded = true
    }

    /**
     * Draw some text with a dark background to the screen at the given
     * position. Font, text color and background color can be overriden.
     * If reverse is 'true' then the box will be drawn to the left
     * of the position as opposed to the right
     */
    private fun drawTextBox(
        text: String,
        position: Vector2,
        font: Font = smallText,
        textFill: Paint = textColor,
        boxFill: Paint = textBg,
        reverse: Boolean = false
    ) {
        val ctx = FUTZ.graphicsContext
        val fontSize: Double = font.size
        val rect = MutableRect(
            x = position.x, y = position.y,
            width = (fontSize * text.length * 0.6) + TEXT_BOX_PADDING * 2.0,
            height = fontSize + TEXT_BOX_PADDING * 2.0
        )
        if (reverse) rect.x -= rect.width

        ctx.fill = boxFill
        ctx.fillRect(rect.x,rect.y,rect.width,rect.height)
        ctx.font = font
        ctx.fill = textFill
        ctx.fillText(text, rect.x + 6.0, rect.y2 - 6.0)
    }

    /** 
     * Draw an immediate-mode button with the given sprite in it at the given position
     * and return whether or not the button was pressed on this frame.
     * Optionally, can display a tooltip.
     * If toggleOn is 'true' a red border will be drawn around the button
     */
    private fun button(
        sprite: Sprite,
        position: Vector2,
        tooltip: String? = null,
        toggleOn: Boolean = false
    ) : Boolean {
        val ctx = FUTZ.graphicsContext
        val mousePos = Input.mouseViewportPosition
        val mouseOver = (
            mousePos.x >= position.x && mousePos.x <= position.x + BUTTON_SIZE &&
            mousePos.y >= position.y && mousePos.y <= position.y + BUTTON_SIZE
        )

        var bg = textBg3
        if (mouseOver)
            bg = if (Input.isPressed(MouseButton.PRIMARY)) textBg else textBg2
        ctx.fill = bg
        ctx.fillRect(position.x, position.y, BUTTON_SIZE, BUTTON_SIZE)
        if (toggleOn) {
            ctx.stroke = Color.CRIMSON
            ctx.lineWidth = 2.0
            ctx.strokeRect(position.x, position.y, BUTTON_SIZE, BUTTON_SIZE)
        }
        ctx.drawImage(
            sprite.image,
            position.x + 3.0, position.y + 3.0,
            BUTTON_SIZE - 6.0, BUTTON_SIZE - 6.0
        )

        tooltip?.let{
            if (mouseOver)
                drawTextBox(text = tooltip, position = mousePos,font = bigText, reverse = true)
        }

        return mouseOver && Input.wasJustReleased(MouseButton.PRIMARY)
    }
}

// Specify the different kinds of messages in the console along with their text color
internal enum class MESSAGE_TYPE(val fill: Paint) {
    NORMAL  (Color.WHITE),
    WARNING (Color.ORANGE),
    ERROR   (Color.RED)
}

// Represents a message in the console
internal data class ConsoleMessage(val text: String, val type: MESSAGE_TYPE) {}