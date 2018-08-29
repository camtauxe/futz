package com.camerontauxe.futz

import javafx.scene.image.Image
import javafx.scene.canvas.GraphicsContext

// The directory (within the JAR file) where images are located
private const val SPRITE_RESOURCE_PATH  = "assets/img/"

// Path to the placeholder image used in the event of an error while
// loading a sprite
private const val ERROR_SPRITE_PATH     = "FUTZ/spriteError.png"

/**
 * Represents an image to display in the game.
 *
 * A sprite simply ties an instance of a JavaFX Image to
 * a value describing how the size of the image (in pixels)
 * relates to the size in the game world (units)
 *
 * Note that the constructor is not publicly visible. The
 * user should instead use the [Assets.loadSprite] factory method
 * to create instances of sprites.
 *
 * @see [Assets.loadSprite]
 *
 * @constructor Creates a new Sprite with the given image
 * and pixels-per-unit ratio
 *
 * @property [image] The JavaFX image this sprite uses
 * @property [pixelsPerUnit] A ratio describing how many pixels
 * in the image correspond to one in-game unit.
 */
public class Sprite internal constructor(
    val image: Image,
    val pixelsPerUnit: Double
) {
    /** The width of the sprite in game-units. (Read-only) */
    val width:  Double = image.width  / pixelsPerUnit
    /** The height of the sprite in game-units. (Read-only) */
    val height: Double = image.height / pixelsPerUnit
}

/**
* Load and return a new sprite asset from the specified file and
* with the specified size
*
* If an error occurs (for example, if the file is not found) then the returned sprite
* is a placeholder image.
*
* Note that this cannot be called before FUTZ has been initialized. To prevent slow-down
* while playing the game, the best time to load sprites is in a custom loading callback
* function which can be specified when calling [FUTZ.init]
*
* @throws IllegalStateException If called before FUTZ has been initialized.
*
* @see [Sprite]
* @see [FUTZ.init]
*
* @param [path] Path to the file to load from. If [fromExternalFile] is 'false',
* this will relative to the "assets/img/" directory within the JAR file. If
* [fromExternalFile] is 'true' then this is path to any file relative to the working
* directory of the application.
* @param [pixelsPerUnit] The pixels-to-game-units ratio of the sprite.
* @receiver Assets
*/
public fun Assets.loadSprite(
    path: String,
    pixelsPerUnit: Double = 100.0,
    fromExternalFile: Boolean = false
): Sprite {
    if (!FUTZ.isRunning)
        throw IllegalStateException("Sprites cannot be loaded before FUTZ is initialized!")

    val url: String =
        if (fromExternalFile)   "file:" + path
        else                    SPRITE_RESOURCE_PATH + path

    val img     = loadImage(url)
    val sprite  = Sprite(img, pixelsPerUnit)
    spritesMutable.add(sprite)
    return sprite
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
    this.drawImage(sprite.image, x, y, sprite.width, sprite.height)
}

/**
 * Extension function for a canvas GraphicsContext to draw a sprite onto the canvas
 * at the given position (given as a vector2).
 *
 * @reciever GraphicsContext
 */
public fun GraphicsContext.drawSprite(sprite: Sprite, position: Vector2) {
    this.drawImage(sprite.image, position.x, position.y, sprite.width, sprite.height)
}

public fun GraphicsContext.drawSpriteWithClip(sprite: Sprite, position: Vector2, clip: Rect) {
    this.drawImage(
        sprite.image,
        clip.x, clip.y, clip.width, clip.height,
        position.x, position.y,
        clip.width / sprite.pixelsPerUnit,
        clip.height / sprite.pixelsPerUnit
    )
}