package com.camerontauxe.futz.prefab

import com.camerontauxe.futz.*

import javafx.scene.canvas.GraphicsContext

open class AnimatedEntity(initialAnimation: Animation) : Entity() {

    val animationPlayer: AnimationPlayer = AnimationPlayer(initialAnimation)

    override var name:          String = "Animated Entity"
    override val collidable:    Boolean = true
    override val hitbox:        Hitbox = Hitbox(
        rect = Rect(
            0.0, 0.0,
            initialAnimation.spriteSheet.spriteWidth,
            initialAnimation.spriteSheet.spriteHeight
        ),
        entity = this
    )

    override fun update() {
        animationPlayer.update()
    }

    override fun draw(ctx: GraphicsContext) {
        ctx.drawAnimation(animationPlayer, Vector2(0.0, 0.0))
    }
}