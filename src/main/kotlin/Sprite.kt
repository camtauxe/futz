package com.camerontauxe.futz

import javafx.scene.image.Image
import javafx.scene.canvas.GraphicsContext

// The directory (within the JAR file) where images are located
private const val SPRITE_RESOURCE_PATH  = "assets/img/"

// Path to the placeholder image used in the event of an error while
// loading a sprite
private const val ERROR_SPRITE_PATH     = "FUTZ/spriteError.png"

public class Sprite (
    val path: String,
    val width: Double,
    val height: Double,
    val loadFromExternalFile: Boolean = false
) {
    public val url =
        if (loadFromExternalFile)   "file:" + path
        else                        SPRITE_RESOURCE_PATH + path

    public var image: Image? = null
        private set 
    public var loaded: Boolean = false
        private set

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
 * at the given position (given as two Doubles).
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