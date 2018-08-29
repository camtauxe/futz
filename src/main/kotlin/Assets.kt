package com.camerontauxe.futz

import javafx.scene.text.Font
import javafx.scene.image.Image
import javafx.scene.canvas.GraphicsContext

// The directory (within the JAR file) where fonts are located
private const val FONT_RESOURCE_PATH    = "assets/font/"
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
 * Loads and manages in-game assets such as Sprites, Fonts and Sounds
 *
 * Has functions to load different kinds of assets from files and lists
 * of all the assets that have been loaded.
 *
 * @TODO: Sounds
 */
public object Assets {

    /** 
     * Whether or not the Liberation font family has been loaded already. (Read-only)
     * Currently, the liberation fonts are loaded by splash screen, so the user
     * can usually expect that this will be true by the time they have control.
     */
    public var liberationFontsLoaded: Boolean = false
        internal set

    // Mutable lists of assets are internally visible only
    // Read-only versions are publically visible
    internal val spritesMutable = mutableListOf<Sprite>()
    internal val fontsMutable   = mutableListOf<Font>()

    /** List of all sprites that have been loaded. (Read-only) */
    public val loadedSprites:   List<Sprite>    = spritesMutable
    /** List of all fonts that have been loaded. (Read-only) */
    public val loadedFonts:     List<Font>      = fontsMutable

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
     */
    public fun loadSprite(
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
     * Load and return a new Font asset from the specified file
     * and of the specified size.
     *
     * If an error occurs (for example, if the file is not found) then the returned
     * font is just the system default.
     *
     * Note that Font assets are really just JavaFX Font objects (unlike sprites, which
     * are a wrapper class for a JavaFX image). So Font loading works the JavaFX way where,
     * once a font is loaded it's family is added to the list of installed font families
     * and new fonts of that family can be created using JavaFX's own Font constructor(s)
     *
     * @param [path] The path to the file to load. If [fromExternalFile] is 'false' this will be
     * a path relative to the "assets/font" directory within the JAR file. If [fromExternalFile]
     * is 'true' then this will be a path relative to the Application's working directory.
     * @param [size] The size of the Font. The best value for this depends on the context
     * in which you intend to draw the font as it will be relative to the scale of the
     * graphics context when it is drawn. If being drawn as an entity, this should be a relatively
     * small value since it will be interpreted as in-game units.
     */
    public fun loadFont(
        path: String,
        size: Double = 0.35,
        fromExternalFile: Boolean = false
    ): Font {
        var url: String? =
            if (fromExternalFile)   path
            else                    FUTZ::class.java.classLoader.getResource(FONT_RESOURCE_PATH+path)?.toExternalForm()

        // There is a chance that the URL returned by the getResource() method will be null
        url?.let{
            val font = Font.loadFont(url,size)
            if (font == null) {
                Debug.error("Error loading font '$url'")
                Debug.warning("The default system font will be used instead.")
                return Font.getDefault()
            }
            this.fontsMutable.add(font)
            return font
        }

        Debug.error("Font resource could not be found at: '$path'")
        Debug.error("Please make sure the font exists in resource directory: $FONT_RESOURCE_PATH")
        Debug.warning("The default system font will be used instead.")
        return Font.getDefault()
    }

    /**
     * Load the family of Liberation fonts. These font files come with FUTZ
     * so they can be guaranteed to be available on any system.
     */
    public fun loadLiberationFonts() {
        //If we've already done this, don't do it again.
        if (liberationFontsLoaded) return
        loadFont("FUTZ/LiberationMono-Regular.ttf")
        loadFont("FUTZ/LiberationMono-Bold.ttf")
        loadFont("FUTZ/LiberationMono-Italic.ttf")
        loadFont("FUTZ/LiberationMono-BoldItalic.ttf")
        loadFont("FUTZ/LiberationSerif-Regular.ttf")
        loadFont("FUTZ/LiberationSerif-Bold.ttf")
        loadFont("FUTZ/LiberationSerif-Italic.ttf")
        loadFont("FUTZ/LiberationSerif-BoldItalic.ttf")
        loadFont("FUTZ/LiberationSans-Regular.ttf")
        loadFont("FUTZ/LiberationSans-Bold.ttf")
        loadFont("FUTZ/LiberationSans-Italic.ttf")
        loadFont("FUTZ/LiberationSans-BoldItalic.ttf")
        liberationFontsLoaded = true
        Debug.log("Liberation Fonts loaded")
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
        position.x, position.y, sprite.width, sprite.height
    )
}