package com.camerontauxe.futz

import javafx.scene.canvas.GraphicsContext

public class SpriteSheet(
    val sprite: Sprite,
    val rows: Int,
    val columns: Int
) {
    val numSprites: Int = rows * columns;
}

public class Animation(
    val spriteSheet: SpriteSheet,
    val startIndex: Int,
    val endIndex: Int,
    val framerate: Double = 24.0,
    val looping: Boolean = true
) {
    val numFrames = endIndex - startIndex + 1
    val frameTime = 1.0 / framerate
}

public class AnimationPlayer (
    initialAnimation: Animation
) {
    var currentAnimation:   Animation = initialAnimation
        private set
    var nextAnimation:      Animation? = null
        private set

    var currentFrame:   Int = 0
        private set
    var runningTime:    Double = 0.0
        private set
    var frameTime:      Double = 0.0
        private set
    var atEnd:          Boolean = false
        private set

    public fun update() {
        addTime(FUTZ.frameTime)
    }

    public fun addTime(seconds: Double) {
        runningTime += seconds
        frameTime   += seconds

        if (frameTime > currentAnimation.frameTime) {
            frameTime = 0.0

            if (atEnd) {
                nextAnimation?.let { setAnimation(it) }
            }
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

    public fun setAnimation(newAnimation: Animation, next: Animation? = null) {
        currentAnimation = newAnimation
        nextAnimation = next
        currentFrame = 0
        runningTime = 0.0
        frameTime = 0.0
        atEnd = false
    }
}

public fun GraphicsContext.drawAnimation(player: AnimationPlayer, position: Vector2) {
    val spriteSheet = player.currentAnimation.spriteSheet
    val sprite = spriteSheet.sprite

    if (!sprite.loaded) sprite.load()

    sprite.image?.let{
        val widthPixels  = it.width  / spriteSheet.columns.toDouble()
        val heightPixels = it.height / spriteSheet.rows.toDouble()
        val frameIndex = player.currentAnimation.startIndex + player.currentFrame
        val clip = Rect(
            x = (frameIndex % spriteSheet.columns) * widthPixels,
            y = (frameIndex / spriteSheet.columns) * heightPixels,
            width  = widthPixels,
            height = heightPixels
        )
        
        drawImage(
            it,
            clip.x, clip.y, clip.width, clip.height,
            position.x, position.y, sprite.width, sprite.height
        )
    }
}