package com.camerontauxe.futz.prefab

import com.camerontauxe.futz.*

import javafx.scene.paint.Paint
import javafx.scene.paint.Color
import javafx.scene.canvas.GraphicsContext

/**
 * A type of entity that is represented with a colored rectangle. Is visible, non-static and
 * collidable.
 *
 * @property rect A rectangle to represent this entity. This rectangle is relative to the
 * position of the entity (So an x,y coordinate of 0,0 means it matches the entity's position).
 * This is the rectangle that is drawn by the enitity's [draw] function and (by default) is the
 * rectangle used for the entity's hitbox.
 * @property fill A JavaFX Paint object representin the color (or gradient) used to draw the
 * entity. Defaults to Color.WHITE
 */
public open class RectEntity(var rect: Rect, var fill: Paint = Color.WHITE) : Entity() {

    override var name:          String = "Rect Entity"
    override val collidable:    Boolean = true
    override var hitbox:        Hitbox = Hitbox(rect = rect.copy(), entity = this)

    override fun draw(ctx: GraphicsContext) {
        ctx.setFill(fill)
        ctx.fillRect(rect)
    }
}