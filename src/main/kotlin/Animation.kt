package com.camerontauxe.futz

import javafx.scene.canvas.GraphicsContext

/**
 * Represents the Sprite Sheet for an animation (an image where all the frames
 * of the animation (or multiple animations) are laid out in a grid).
 * @property sprite The Sprite representing the entire sprite sheet
 * @property rows The number of rows the sprite sheet has.
 * @property columns The number of columns the sprite sheet has.
 * @see Animation
 */
public class SpriteSheet(
    val sprite:     Sprite,
    val rows:       Int,
    val columns:    Int
) {
    /**
     * The total number of individual sprites/frames on the sprite sheet.
     */
    val numSprites: Int = rows * columns;
}

/**
 * Represents a single animation on a sprite sheet as a sequence of frames
 * on the sheet.
 * @property spriteSheet the SpriteSheet that this animation refers to.
 * @property startIndex the index of the first frame of this animation on the
 * sprite sheet. The index is counted starting at 0 and is read left-to-right,
 * top-to-bottom across the sprite sheet.
 * @property endIndex The index of the last frame of this animation on the
 * sprite sheet. The index is counted starting at 0 and is read left-to-right,
 * top-to-bottom across the sprite sheet.
 * @property framerate The playback speed (in frames per second) of this animation
 * Defaults to 24
 * @property looping Whether or not this animation is meant to loop endlessly.
 * Defaults to true.
 * @see SpriteSheet
 * @see AnimationPlayer
 */
public class Animation(
    val spriteSheet:    SpriteSheet,
    val startIndex:     Int,
    val endIndex:       Int,
    val framerate:      Double = 24.0,
    val looping:        Boolean = true
) {
    /**
     * The total number of frames in the animation
     */
    val numFrames = endIndex - startIndex + 1
    /**
     * The time (in seconds) that each frame of the animation lasts.
     */
    val frameTime = 1.0 / framerate
}

/**
 * Controls the playback of animations. Keeps track of which frame of an
 * animation should currently be displayed.
 * @param initialAnimation The animation that this AnimationPlayer will
 * start with.
 * @see Animation
 */
public class AnimationPlayer (
    initialAnimation: Animation
) {
    /**
     * The animation currently being displayed. (Read-only).
     * Please use [setAnimation] to change this.
     */
    var currentAnimation:   Animation = initialAnimation
        private set
    /**
     * The animation to switch to once the current animation has finished
     * (unless the current animation is looping). (Read-only). Please use
     * [setAnimation] to change this.
     */
    var nextAnimation:      Animation? = null
        private set

    /**
     * The index of the frame of the current animation that is currently
     * being displayed. (Read-only)
     */
    var currentFrame:   Int = 0
        private set
    /**
     * The time (in seconds) that the current animation has been running. (Read-only)
     */
    var runningTime:    Double = 0.0
        private set
    /**
     * The time (in seconds) that the animation has been in its current frame. (Read-only)
     */
    var frameTime:      Double = 0.0
        private set
    /**
     * Whether or not the current animation has reached its end. This will only
     * become true for a non-looping animation. (Read-only)
     */
    var atEnd:          Boolean = false
        private set

    /**
     * Progress the animation by the current FUTZ frame time.
     * @see [FUTZ.frameTime]
     */
    public fun update() {
        addTime(FUTZ.frameTime)
    }

    /**
     * Progress the animation by some time in seconds.
     */
    public fun addTime(seconds: Double) {
        runningTime += seconds
        frameTime   += seconds

        // Switch to next frame if it is time to do so.
        if (frameTime > currentAnimation.frameTime) {
            frameTime = 0.0

            // If there is a next animation and we're at the end of this one,
            // switch to the next animation
            if (atEnd) {
                nextAnimation?.let { setAnimation(it) }
            }
            // otherwise, increment the frame of the current animation
            else {
                if (currentFrame == currentAnimation.numFrames-1) {
                    if (currentAnimation.looping)
                        currentFrame = 0
                    else
                        atEnd = true
                }
                else
                    currentFrame++
            }
        }
    }

    /**
     * Set the currently playing animation.
     * @param newAnimation the Animation to switch to.
     * @param next If newAnimation is not a looping animation, then the
     * player will automatically switch to this animation once newAnimation has
     * finished. If it remains null, newAnimation will simply freeze on the last
     * frame once it's done.
     */
    public fun setAnimation(newAnimation: Animation, next: Animation? = null) {
        currentAnimation = newAnimation
        nextAnimation = next
        currentFrame = 0
        runningTime = 0.0
        frameTime = 0.0
        atEnd = false
    }
}

/**
 * Draw the current frame of the given animatonPlayer at the given position.
 * If the animation's sprite sheet has not yet been loaded, it will be loaded
 * now.
 * @see AnimationPlayer
 */
public fun GraphicsContext.drawAnimation(player: AnimationPlayer, position: Vector2) {
    val spriteSheet = player.currentAnimation.spriteSheet
    val sprite = spriteSheet.sprite

    if (!sprite.loaded) sprite.load()

    sprite.image?.let{
        // determine the clip of the sprite sheet for the current frame
        val widthPixels  = it.width  / spriteSheet.columns.toDouble()
        val heightPixels = it.height / spriteSheet.rows.toDouble()
        val frameIndex = player.currentAnimation.startIndex + player.currentFrame
        val clip = Rect(
            x = (frameIndex % spriteSheet.columns) * widthPixels,
            y = (frameIndex / spriteSheet.columns) * heightPixels,
            width  = widthPixels,
            height = heightPixels
        )
        
        // draw the sprite clipped to the current frame
        drawImage(
            it,
            clip.x, clip.y, clip.width, clip.height,
            position.x, position.y, sprite.width, sprite.height
        )
    }
}