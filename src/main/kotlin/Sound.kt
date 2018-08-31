package com.camerontauxe.futz

import javafx.scene.media.AudioClip
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration

private const val SOUND_RESOURCE_PATH = "assets/sound/"
private const val MUSIC_RESOURCE_PATH = "assets/music/"

public class Sound (
    val path: String,
    val defaultVolume: Double = 1.0,
    val loadFromExternalFile: Boolean = false
) {
    public val url =
        if (loadFromExternalFile)   "file:" + path
        else                        SOUND_RESOURCE_PATH + path

    public var clip: AudioClip? = null
        private set
    public var loaded: Boolean = false
        private set

    public fun load() {
        if (!FUTZ.isRunning)
            throw IllegalStateException("Sounds cannot be loaded before FUTZ is initialized!")
        if (loaded) return

        try {
            clip = AudioClip(url)
            loaded = true
        } catch (e: Exception) {
            Debug.error("Could not load sound '$url'")
        }
    }

    public fun play(volume: Double = 1.0) {
        if (!loaded) load()

        clip?.let {
            it.play(defaultVolume * volume)
        }
    }
}

public class Music (
    val path: String,
    val defaultVolume: Double = 1.0,
    var looping: Boolean = true,
    val loadFromExternalFile: Boolean = false
) {
    public val url =
        if (loadFromExternalFile)   path
        else                        FUTZ::class.java.classLoader.getResource(MUSIC_RESOURCE_PATH+path)?.toExternalForm()

    public var media: Media? = null
        private set
    public var player: MediaPlayer? = null
        private set
    public var loaded: Boolean = false
        private set
    public var isPlaying: Boolean = false
        private set

    public fun load() {
        if (!FUTZ.isRunning)
            throw IllegalStateException("Music cannot be loaded before FUTZ is initialized!")
        if (loaded) return

        // There is a chance that the URL returned by getResource() will be null
        url?.let{
            try {
                media = Media(url)
                player = MediaPlayer(media)
                player?.let {
                    it.volume = defaultVolume
                    it.onEndOfMedia = object : Runnable {
                        override fun run() {
                            if (looping) it.seek(Duration.ZERO)
                        }
                    }
                } // end player?.let
                loaded = true
            } catch (e: Exception) {
                Debug.error("Error loading music '$url'")
            }
            return
        }

        // If we get here, it means the url was null
        Debug.error("Music resource could not be found at: '$path'")
        Debug.error("Please make sure the music exists in resource directory: $MUSIC_RESOURCE_PATH")
    }

    public fun play() {
        if (!loaded) load()

        player?.let {
            it.seek(Duration.ZERO)
            it.play()
            isPlaying = true
        }
    }

    public fun stop() {
        if (!isPlaying) return

        player?.let {
            it.stop()
            isPlaying = false
        }
    }
}