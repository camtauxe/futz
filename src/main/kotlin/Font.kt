package com.camerontauxe.futz

import javafx.scene.text.Font

// The directory (within the JAR file) where fonts are located
private const val FONT_RESOURCE_PATH    = "assets/font/"

public object Fonts {

    public var liberationFontsLoaded: Boolean = false
        private set

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
    * @receiver Assets
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
    * @receiver Assets
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