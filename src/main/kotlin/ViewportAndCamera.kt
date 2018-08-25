package com.camerontauxe.futz

import javafx.scene.canvas.GraphicsContext

// The size of game-unit as a ratio of the width of the viewport
private const val UNIT_SIZE:        Double = 0.1
// The number of game-units that cover the width of the viewport
private const val UNITS_PER_SCREEN: Double = 1.0 / UNIT_SIZE

/**
 * Represents the screen where the game is rendered.
 *
 * The Viewport contains properties and functions pertaining to relating the game window
 * and screen to an area within the game-world.
 */
public object Viewport {

    /** The aspect ratio of the viewport (width / height) (Read-only) */
    public var aspectRatio: Double = 1.0
        private set
    /**
     * The rectangle representing the viewport as it resides in the game window.
     * Width and height are in pixels and the x,y position represents the postion (in pixels)
     * relative to the upper-left-hand corner of the window. (Read-only)
     */
    public lateinit var windowRect: Rect
        private set
    /**
     * The rectangle representing the viewport. Width and height are in pixels
     * and the x,y position is always (0,0) (Read-only)
     */
    public lateinit var rect:       Rect
        private set

    /** The width of the viewport (in pixels) (Read-only) */
    public val width:          Double get() = rect.width
    /** The height of the viewport (in pixels) (Read-only) */
    public val height:         Double get() = rect.height
    /** The width of the viewport (in game-units) (accounts for camera zoom) (Read-only) */
    public val widthUnits:     Double get() = UNITS_PER_SCREEN / Camera.zoom
    /** The height of the viewport (in game-units) (accounts for camera zoom) (Read-only) */
    public val heightUnits:    Double get() = UNITS_PER_SCREEN / Camera.zoom / aspectRatio

    /**
     * The viewport represented in game-units. This takes into account the current position
     * and zoom level of the camera, and is centered around the camera. The x,y position will
     * represent the world-space coordinate visible in the upper-left-hand corner of the screen.
     */
    public val worldRect: Rect 
        get() = Rect(
            x       =   -(widthUnits / 2.0) + Camera.position.x,
            y       =   -(heightUnits / 2.0) + Camera.position.y,
            width   =   widthUnits,
            height  =   heightUnits
        )

    /**
     * The viewport represented in game-units not taking into account the position and zoom
     * level of the camera. This is always centered around the world-origin.
     */
    public val worldRectOrigin: Rect
        get() = Rect(
            x       =   -(UNITS_PER_SCREEN / 2.0),
            y       =   -(UNITS_PER_SCREEN / 2.0 / aspectRatio),
            width   =   UNITS_PER_SCREEN,
            height  =   UNITS_PER_SCREEN / aspectRatio
        )

    /**
     * Convert a value in pixels to a value in game-units. By default this, takes into
     * account the current zoom level of the camera. If [unZoomed] is 'true' the current
     * zoom level is ignored and 1.0 is assumed.
     * This is the inverse of [unitsToPixels]
     * @see unitsToPixels
     */
    public fun pixelsToUnits(pixelValue: Double, unZoomed: Boolean = false): Double =
        pixelValue / width * UNITS_PER_SCREEN / (if (unZoomed) 1.0 else Camera.zoom)

    /**
     * Convert a value in game-units to a value in pixels. By default this, takes into
     * account the current zoom level of the camera. If [unZoomed] is 'true' the current
     * zoom level is ignored and 1.0 is assumed.
     * This is the inverse of [pixelsToUnits]
     * @see pixelsToUnits
     */
    public fun unitsToPixels(unitValue: Double, unZoomed: Boolean = false): Double =
        unitValue * width / UNITS_PER_SCREEN * (if (unZoomed) 1.0 else Camera.zoom)

    /**
     * Convert a position in the game-world (in game-units) to a position in the viewport
     * (in pixels). If the given position is out-of-sight of the camera, the returned position
     * will naturally lie outside the viewport's rectangle.
     */
    public fun worldPosToViewportPos(pos: Vector2): Vector2 =
        Vector2(
            x = (pos.x + (widthUnits / 2.0) - Camera.position.x) 
                / widthUnits * width,
            y = (pos.y + (heightUnits / 2.0) - Camera.position.y)
                / widthUnits * width
        )
    /**
     * Convert a position in the game-world (in game-units) to a position in the viewport
     * (in pixels). If the given position is out-of-sight of the camera, the returned position
     * will naturally lie outside the viewport's rectangle.
     */
    public fun worldPosToViewportPos(x: Double, y: Double): Vector2 =
        worldPosToViewportPos(Vector2(x, y))

    /**
     * Convert a position in the viewport (in pixels) to a position in the 
     * game-world (in game-units)
     */
    public fun viewportPosToWorldPos(pos: Vector2): Vector2 =
        Vector2(
            x = pos.x / width * widthUnits
                - (widthUnits / 2.0) + Camera.position.x,
            y = pos.y / width * widthUnits
                - (heightUnits / 2.0) + Camera.position.y
        )
    /**
     * Convert a position in the viewport (in pixels) to a position in the 
     * game-world (in game-units)
     */
    public fun viewportPosToWorldPos(x: Double, y: Double): Vector2 =
        viewportPosToWorldPos(Vector2(x, y))

    /**
     * Update the viewport rectangle to fit within a window with the given dimensions.
     * The resulting viewport rectangle will be the largest possible rectangle with the
     * set aspect ratio that can fit inside the window (centered).
     */
    internal fun updateWindowSize(windowWidth: Double, windowHeight: Double) {
        if (windowWidth < windowHeight * aspectRatio) {
            val height = (1.0 / aspectRatio) * windowWidth
            windowRect = Rect(
                x = 0.0, y = (windowHeight / 2.0) - (height / 2.0),
                width = windowWidth, height = height
            )
        }
        else {
            val width = windowHeight * aspectRatio
            windowRect = Rect(
                x = (windowWidth / 2.0) - (width / 2.0), y = 0.0,
                width = width, height = windowHeight
            )
        }
        rect = Rect(x = 0.0, y = 0.0, width = windowRect.width, height = windowRect.height)
    }

    /**
     * Initialize the aspect ratio and viewport to fit inside a window with the given
     * dimensions.
     */
    internal fun initInWindow(windowWidth: Double, windowHeight: Double) {
        aspectRatio = windowWidth / windowHeight
        updateWindowSize(windowWidth, windowHeight)
    }
}

/**
 * The Camera object represents the in-game camera. The viewport is rendered around the camera's
 * position and is scaled according to the camera's zoom level.
 */
public object Camera {
    /**
     * The current position of the camera (in game-units, relative to the world-origin). The
     * viewport is positioned with this point in the center of it.
     *
     * Note that setting the position to another Vector2 copies the given Vector2 and
     * does not set the position as a reference to it.
     */
    public var position: MutableVector2 = MutableVector2(0.0, 0.0)
        set(value) {field = value.mutableCopy()}
    /**
     * The zoom ratio of the camera. The viewport is scaled according to this value.
     * The larger the value, the more zoomed in. (So at 2.0, everything looks twice as big,
     * at 0.5 everything appears half as big. 1.0 is normal)
     */
    public var zoom:     Double         = 1.0
}

// Extension functions

// Transform graphics context between world-space and viewport

/**
 * Transform this graphics context (assuming it is currently transformed relative to the viewport)
 * into world-space. (0,0 will draw at the world-origin and distances drawn are in game-units)
 * This is the inverse of [GraphicsContext.transformWorldSpaceToViewport]
 * @see GraphicsContext.transformWorldSpaceToViewport
 * @receiver GraphicsContext
 */
public fun GraphicsContext.transformViewportToWorldSpace() {
    val scaleFactor = Viewport.width * UNIT_SIZE * Camera.zoom
    this.scale(scaleFactor, scaleFactor)
    this.translate(
        (Viewport.widthUnits / 2.0) - Camera.position.x,
        (Viewport.heightUnits / 2.0) - Camera.position.y
    )
}

/**
 * Transform this graphics context (assuming it is currently transformed relative to world-space)
 * to the viewport. (0,0 will draw in the upper-left-hand corner of the screen and distances
 * drawn are in pixels)
 * This is the inverse of [GraphicsContext.transformViewportToWorldSpace]
 * @see GraphicsContext.transformViewportToWorldSpace
 * @receiver GraphicsContext
 */
public fun GraphicsContext.transformWorldSpaceToViewport() {
    val scaleFactor = UNITS_PER_SCREEN / Viewport.width / Camera.zoom
    this.scale(scaleFactor, scaleFactor)
    this.translate(
        (Viewport.width / 2.0) + Camera.position.x,
        (Viewport.height / 2.0) + Camera.position.y
    )
}

// Transform Vector2's between world-space and viewport

/**
 * Convert this Vector2 from a position on the viewport to a position in
 * the game-world (in game-units).
 * This is equivalent to calling [Viewport.viewportPosToWorldPos] with this as the argument.
 * This is the inverse of [Vector2.worldSpaceToViewport]
 * @see Viewport.viewportPosToWorldPos
 * @see Vector2.worldSpaceToViewport
 */
public fun Vector2.viewportToWorldSpace(): Vector2 = Viewport.viewportPosToWorldPos(this)
/**
 * Convert this Vector2 from a position in the game-world to a position on the viewport (in pixels).
 * This is equivalent to calling [Viewport.worldPosToViewportPos] with this as the argument.
 * This is the inverse of [Vector2.viewportToWorldSpace]
 * @see Viewport.worldPosToViewportPos
 * @see Vector2.viewportToWorldSpace
 * @receiver Vector2
 */
public fun Vector2.worldSpaceToViewport(): Vector2 = Viewport.worldPosToViewportPos(this)

// Transform Rects between world-space and viewport

/**
 * Transform this Rect from an area of the viewport (in pixels) to an area of the game-world 
 * (where the position is relative to the world-origin and width/height are in game-units.)
 * This is the inverse of [Rect.worldSpaceToViewport]
 * @see Rect.worldSpaceToViewport
 * @receiver Rect
 */
public fun Rect.viewportToWorldSpace(): Rect = 
    Rect(
        position = this.position.viewportToWorldSpace(),
        width = Viewport.pixelsToUnits(this.width),
        height = Viewport.pixelsToUnits(this.height)
    )
/**
 * Transform this Rect from an area of the game-world (in game-units) to an aera of the viewport
 * (where the position is relative to upper-left-hand corner of the screen and the width/height
 * are in game-units).
 * This is the inverse of [Rect.viewportToWorldSpace]
 * @see Rect.viewportToWorldSpace
 * @receiver Rect
 */
public fun Rect.worldSpaceToViewport(): Rect =
    Rect(
        position = this.position.worldSpaceToViewport(),
        width = Viewport.unitsToPixels(this.width),
        height = Viewport.unitsToPixels(this.height)
    )