package com.camerontauxe.futz

import javafx.animation.AnimationTimer

/**
 * An extension of the JavaFX AnimationTimer that manages timing of frames for FUTZ.
 */
internal object Timer : AnimationTimer() {

    // The system time (in nanoseconds of the previous frame)
    private var then: Long = 0L

    // The time (in seconds) of the duration of the previous frame
    internal var frameTime: Double = 0.0
    // The time (in milliseconds) that FUTZ has been running so far
    internal var gameTime: Long = 0L
    // The number of frames since FUTZ began
    internal var frame: Long = 0L
    // The time (in milliseconds) since the scene was last changed
    internal var sceneTime: Long = 0L
    // The number of frames since the scene was last changed
    internal var sceneFrame: Long = 0L

    /**
     * Called by JavaFX 60 times per second.
     */
    override fun handle(now: Long) {
        //then and now are equal on first frame
        if (then == 0L) {then = now;}
        
        frameTime = (now - then).toDouble() / 1_000_000_000.0

        gameTime += (frameTime*1000).toLong()
        frame++

        sceneTime += (frameTime*1000).toLong()
        sceneFrame++

        FUTZ.doFrame()

        then = now
    }

    internal fun resetSceneTime() {
        sceneTime = 0L
        sceneFrame = 0L
    }
}