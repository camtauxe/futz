package com.camerontauxe.futz

import javafx.scene.canvas.GraphicsContext

public class SpriteSheet(
    val sprite: Sprite,
    val rows: Int,
    val columns: Int
) {
    val numSprites: Int = rows * columns;

    val spriteWidth:        Double = sprite.width  / columns.toDouble()
    val spriteHeight:       Double = sprite.height / rows.toDouble()
    val spriteWidthPixels:  Double = sprite.image.width  / columns.toDouble()
    val spriteHeightPixels: Double = sprite.image.height / rows.toDouble()

    fun getClip(index: Int): Rect =
        getClip(columnIndex = index % rows, rowIndex = index / rows)

    fun getClip(columnIndex: Int, rowIndex: Int): Rect =
        Rect(
            x = columnIndex * spriteWidthPixels,
            y = rowIndex * spriteHeightPixels,
            width = spriteWidthPixels,
            height = spriteHeightPixels
        )
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
    var atEnd:         Boolean = false
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

    public fun getSpriteSheetClip(): Rect =
        currentAnimation.spriteSheet.getClip(
            currentAnimation.startIndex + currentFrame
        )

}

public fun GraphicsContext.drawAnimation(player: AnimationPlayer, position: Vector2) {
    this.drawSpriteWithClip(
        player.currentAnimation.spriteSheet.sprite,
        position,
        player.currentAnimation.spriteSheet.getClip(
            player.currentAnimation.startIndex + player.currentFrame
        )
    )
}