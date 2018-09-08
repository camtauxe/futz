package com.camerontauxe.futz

import javafx.scene.image.Image
import javafx.scene.canvas.GraphicsContext

// The directory (within the JAR file) where images are located
private const val SPRITE_RESOURCE_PATH  = "assets/img/"

// Path to the placeholder image used in the event of an error while
// loading a sprite
private const val ERROR_SPRITE_PATH     = "FUTZ/spriteError.png"

/**
 * Represents an image that can be displayed in-game.
 *
 * A sprite can be initialized before it is loaded. It will be loaded
 * automatically the first time it is drawn. But loading can take some time,
 * so it is recommended for performance to load sprites ahead of time
 * using the [load] function.
 *
 * @property path The path to the image file for this sprite. If [loadFromExternalFile]
 * is false, this will be a path relative to the "assets/img" directory
 * within the JAR file. If [loadFromExternalFile] is true, this will be a path
 * relative to the application's working directory.
 * @property width The width of this sprite. The units for this depends on the context
 * in which the sprite will be drawn. If drawn as an entity, this will represent
 * a length in game-units. If this sprite is going to be used as a sprite sheet,
 * it represents the width of a single frame on the sprite sheet and not the total
 * size of the sheet.
 * @property height The height of this sprite. The units for this depends on the context
 * in which the sprite will be drawn. If drawn as an entity, this will represent
 * a length in game-units. If this sprite is going to be used as a sprite sheet,
 * it represents the height of a single frame on the sprite sheet and not the total
 * size of the sheet.
 * @property loadFromExternalFile Whether or not the given path represents
 * a path within the JAR's img asset directory or a file elsewhere. See the
 * description for [path] to see how exactly this affects the way [path] is
 * interpreted.
 */
public class Sprite (
    val path:   String,
    val width:  Double,
    val height: Double,
    val loadFromExternalFile: Boolean = false
) {
    /**
     * The url that the image file will be loaded from. This
     * depends on [path] and [loadFromExternalFile]
     */
    public val url =
        if (loadFromExternalFile)   "file:" + path
        else                        SPRITE_RESOURCE_PATH + path

    /**
     * The JavaFX Image object representing this sprite.
     */
    public var image: Image? = null
        private set
    /**
     * Whether or not this sprite has been loaded.
     */
    public var loaded: Boolean = false
        private set

    /**
     * Load the sprite from its file. If an error occurs (for example,
     * if the file could not be found), then an error will be shown in the
     * FUTZ debug console and the sprite shown will be a default "error" sprite.
     *
     * @throws IllegalStateException If called before FUTZ has been initialized
     */
    public fun load() {
        if (!FUTZ.isRunning)
            throw IllegalStateException("Sprites cannot be loaded before FUTZ is initialized!")
        if (loaded) return

        image = loadImage(url)

        loaded = true
    }
}

/**
 * Load a JavaFX image from the given URL.
 * Return the error placeholder image if the URL is invalid
 */
private fun loadImage(url: String) : Image {
    try {
        return Image(url, false)
    } catch (e: IllegalArgumentException) {
        Debug.error("Could not load sprite image '$url'")
        return getErrorImage()
    }
}

/** Load the error placeholder image */
private fun getErrorImage(): Image = Image(SPRITE_RESOURCE_PATH+ERROR_SPRITE_PATH, false)

/**
 * Extension function for a canvas GraphicsContext to draw a sprite onto the canvas
 * at the given position (given as two Doubles). If the sprite has not
 * yet been loaded, it will be loaded now.
 *
 * @receiver GraphicsContext
 */
public fun GraphicsContext.drawSprite(sprite: Sprite, x: Double, y: Double) {
    if (!sprite.loaded) sprite.load()

    sprite.image?.let{
        this.drawImage(
            sprite.image, x, y, sprite.width, sprite.height)
    }
}

/**
 * Extension function for a canvas GraphicsContext to draw a sprite onto the canvas
 * at the given position (given as a vector2).
 *
 * @reciever GraphicsContext
 */
public fun GraphicsContext.drawSprite(sprite: Sprite, position: Vector2) {
    this.drawSprite(sprite, position.x, position.y)
}

/**
 * Extension function for a canvas GraphicsContext to draw a part of a sprite onto the canvas
 * at the given position and clipped according to the given rectangle.
 * If the sprite has not yet been loaded, it will be loaded now. Note that the
 * values for the clip rectangle will always be interpreted as pixels regardless
 * the units for the sprites width and height.
 *
 * @receiver GraphicsContext
 */
public fun GraphicsContext.drawSpriteWithClip(sprite: Sprite, position: Vector2, clip: Rect) {
    if (!sprite.loaded) sprite.load()

    sprite.image?.let{
        this.drawImage(
            it,
            clip.x, clip.y, clip.width, clip.height,
            position.x, position.y,
            sprite.width,
            sprite.height
        )
    }
}