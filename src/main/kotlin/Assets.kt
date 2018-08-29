package com.camerontauxe.futz

import javafx.scene.text.Font

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

}
