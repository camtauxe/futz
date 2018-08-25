package com.camerontauxe.futz.prefab

import com.camerontauxe.futz.*

import javafx.scene.canvas.GraphicsContext

/**
 * A type of entity that is represented by a single Sprite. Is visible, non-static and
 * collidable.
 *
 * @property sprite A Sprite used to represent this entity. This is the sprite that is
 * drawn by the entity's [draw] function and the sprite's size is (by default) used for
 * the size of the entity's hitbox.
 */
open class SpriteEntity(var sprite: Sprite) : Entity() {

    override var name:          String = "Sprite Entity"
    override val collidable:    Boolean = true
    override val hitbox:        Hitbox =
        Hitbox(rect = Rect(0.0, 0.0, sprite.width, sprite.height), entity = this)

    override fun draw(ctx: GraphicsContext) {
        ctx.drawSprite(sprite, 0.0, 0.0)
    }
}