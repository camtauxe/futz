package com.camerontauxe.futz

import javafx.scene.media.AudioClip
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration

// The directory (within the JAR file) where sounds are stored
private const val SOUND_RESOURCE_PATH = "assets/sound/"
// The directory (within the JAR file) where music files are stored
private const val MUSIC_RESOURCE_PATH = "assets/music/"

/**
 * Represents a sound-effect that can be played in-game.
 *
 * Sounds can be initialized before their file is actually loaded. They will
 * automatically be loaded the first time they are played, but loading can take
 * some time, so it is recommended to load the sound effect ahead of time using
 * the [load] function.
 *
 * @property path The path to the file for this sound effect.
 * If [loadFromExternalFile]is false, this will be a path relative to the
 * "assets/sound" directory within the JAR file. If [loadFromExternalFile]
 * is true, this path will be any acceptable java URI.
 * @property defaultVolume The default volume (as a value from 0.0 to 1.0,
 * where 0.0 is mute and 1.0 is full volume) that this sound will play at.
 * The volume can be adjusted further when the sound is played using a parameter
 * on the [play] function.
 * @property loadFromExternalFile Whether or not the given path represents
 * a path within the JAR's sound asset directory or a file elsewhere. See the
 * description for [path] to see how exactly this affects the way [path] is
 * interpreted.
 */
public class Sound (
    val path:           String,
    val defaultVolume:  Double = 1.0,
    val loadFromExternalFile: Boolean = false
) {
    /**
     * The fully-realized URL of the sound effect file. If [loadFromExternalFile]
     * is true, this will be equivalent to [path], otherwise it will be a jar 
     * URI pointing to the file within the JAR file.
     */
    public val url =
        if (loadFromExternalFile)   path
        else                        FUTZ::class.java.classLoader.getResource(SOUND_RESOURCE_PATH+path)?.toExternalForm()

    /**
     * The JavaFX AudioClip object used internally by this sound effect. (Read-only)
     *
     * This will be null before the sound effect has been loaded. Afterwards,
     * it can be manipulated for more control over this sound effect.
     */
    public var clip: AudioClip? = null
        private set

    /**
     * Whether or not this sound effect has been loaded yet (Read-only)
     */
    public var loaded: Boolean = false
        private set

    /**
     * Load the sound effect from its file. If an error occurs (for example,
     * if the file could not be found), then an error will be shown in the
     * FUTZ debug console and [loaded] will remain false.
     *
     * @throws IllegalStateException If called before FUTZ has been initialized
     */
    public fun load() {
        if (!FUTZ.isRunning)
            throw IllegalStateException("Sounds cannot be loaded before FUTZ is initialized!")
        if (loaded) return

        // There is a chance that the URL returned by getResource() will be null
        url?.let {
            try {
                clip = AudioClip(url)
                loaded = true
            } catch (e: Exception) {
                Debug.error("Could not load sound '$url'")
                e.printStackTrace()
            }
            return
        }

        // If we get here, it means the url was null
        Debug.error("Sound resource could not be found at: '$path'")
        Debug.error("Please make sure the sound exists in resource directory: $SOUND_RESOURCE_PATH")
    }

    /**
     * Play this sound effect.
     *
     * If the sound effect has not yet been loaded, it will be loaded now.
     * @param volume A multiplier applied to [defaultVolume] to get the actual
     * volume to play the sound effect at. Note that the sound effect can never
     * be played louder than its full volume. Defaults to 1.0
     */
    public fun play(volume: Double = 1.0) {
        if (!loaded) load()

        clip?.let {
            it.play(defaultVolume * volume)
        }
    }
}

/**
 * Represents background music that can be played in-game.
 *
 * Music can be initialized before its file is actually loaded. It will
 * automatically be loaded the first time it is played, but loading can take
 * some time, so it is recommended to load the music ahead of time using
 * the [load] function.
 *
 * Internally, Music uses the JavaFX Media and MediaPlayer classes, which
 * can be pretty buggy. But there's nothing I can do about it unless I write
 * my own media streaming system, which I don't wanna.
 *
 * @property path The path to the file for this music.
 * If [loadFromExternalFile]is false, this will be a path relative to the
 * "assets/music" directory within the JAR file. If [loadFromExternalFile]
 * is true, this path will be any acceptable java URI.
 * @property defaultVolume The default volume (as a value from 0.0 to 1.0,
 * where 0.0 is mute and 1.0 is full volume) that this music will play at.
 * @property looping whether or not this music should loop.
 * @property loadFromExternalFile Whether or not the given path represents
 * a path within the JAR's music asset directory or a file elsewhere. See the
 * description for [path] to see how exactly this affects the way [path] is
 * interpreted.
 */
public class Music (
    val path:           String,
    val defaultVolume:  Double = 1.0,
    var looping:        Boolean = true,
    val loadFromExternalFile: Boolean = false
) {
    /**
     * The fully-realized URL of the music file. If [loadFromExternalFile]
     * is true, this will be equivalent to [path], otherwise it will be a jar 
     * URI pointing to the file within the JAR file.
     */
    public val url =
        if (loadFromExternalFile)   path
        else                        FUTZ::class.java.classLoader.getResource(MUSIC_RESOURCE_PATH+path)?.toExternalForm()

    private var media: Media? = null
    private var player: MediaPlayer? = null

    /**
     * Whether or not this music has been loaded yet. (Read-Only)
     */
    public var loaded: Boolean = false
        private set
    /**
     * Whether or not this music is currently playing. (Read-only).
     * Please use [play] or [stop] functions to control playback.
     */
    public var isPlaying: Boolean = false
        private set

    /**
     * Load the music from its file. If an error occurs (for example,
     * if the file could not be found), then an error will be shown in the
     * FUTZ debug console and [loaded] will remain false.
     *
     * @throws IllegalStateException If called before FUTZ has been initialized
     */
    public fun load() {
        if (!FUTZ.isRunning)
            throw IllegalStateException("Music cannot be loaded before FUTZ is initialized!")
        if (loaded) return

        // There is a chance that the URL returned by getResource() will be null
        url?.let{
            try {
                media = Media(url)
                player = MediaPlayer(media)
                // If the player was initialized successfully,
                // set some of its properties
                player?.let {
                    it.volume = defaultVolume
                    // Loop
                    it.onEndOfMedia = object : Runnable {
                        override fun run() {
                            if (looping) it.seek(Duration.ZERO)
                        }
                    }
                    it.onError = object : Runnable {
                        override fun run() {
                            Debug.error("Error occured with music '$path'")
                            Debug.error(it.error.toString())
                        }
                    }
                    loaded = true
                } // end player?.let
            } catch (e: Exception) {
                Debug.error("Error loading music '$url'")
                e.printStackTrace()
            }
            return
        }

        // If we get here, it means the url was null
        Debug.error("Music resource could not be found at: '$path'")
        Debug.error("Please make sure the music exists in resource directory: $MUSIC_RESOURCE_PATH")
    }

    /**
     * Play the music. If the music has not yet been loaded, it will be loaded
     * now. If the music is already playing, it will be restarted from the
     * beginning.
     */
    public fun play() {
        if (!loaded) load()

        player?.let {
            // it.seek(Duration.ZERO)
            it.play()
            isPlaying = true
        }
    }

    /**
     * Stop the music if it is playing.
     */
    public fun stop() {
        if (!isPlaying) return

        player?.let {
            it.stop()
            isPlaying = false
        }
    }
}