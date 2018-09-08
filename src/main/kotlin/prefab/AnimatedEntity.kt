package com.camerontauxe.futz.prefab

import com.camerontauxe.futz.*

import javafx.scene.canvas.GraphicsContext

/**
 * A type of entity that is represented by an animated sprite. Is visible, non-static
 * and collidable.
 *
 * @property initialAnimation The animation that this sprite will initally
 * display. This can be changed later using the entity's [AnimationPlayer],
 * but the size of a sprite in this initial animation is (by default) used to
 * determine the size of the entity's hitbox.
 * @see Animation
 & @see AnimationPlayer
 */
open class AnimatedEntity(initialAnimation: Animation) : Entity() {

    /**
     * This entity's animation player.
     */
    public val animationPlayer: AnimationPlayer = AnimationPlayer(initialAnimation)

    override var name:          String = "Animated Entity"
    override val collidable:    Boolean = true
    override val hitbox:        Hitbox = Hitbox(
        rect = Rect(
            0.0, 0.0,
            initialAnimation.spriteSheet.sprite.width,
            initialAnimation.spriteSheet.sprite.height
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