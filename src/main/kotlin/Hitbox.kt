package com.camerontauxe.futz

/**
 * A hitbox represents a rectangle that is tied to a specific entity. Used in collision detection.
 * @property entity The entity that this hitbox is attached to
 */
public class Hitbox(rect: Rect, var entity: Entity) {

    /**
     * The rectangle representing this hitbox. Note that is interpreted with
     * a position relative to the entity the hitbox is attached to and with
     * width and height being in game-units. For example: A rectangle with
     * the values (x: 0, y: 0, width: 3, height: 4) represents a hitbox that
     * is 3 game-units wide and 4 game-units tall with its upper-left-hand corner
     * at exactly the current positon of the entity it is attached to.
     *
     * Note that setting the rect to another [Rect] copies the given Rect and
     * does not set rect as a reference to it (Similar to [Entity.position]).
     */
    public var rect: MutableRect = rect.mutableCopy()
        set(value) {field = value.mutableCopy()}

    /**
     * This hitbox's rectangle in game-world space. (Width and height will
     * still be in game-units but the position is now the global position of
     * hitbox (as opposed to relative to the entity)) (Read-only)
     */
    public val worldRect: Rect get() = rect.entitySpaceToWorldSpace(entity)

    /**
     * This htibox's rectangle in viewport-space. (Width and height are
     * now in pixels and the position is relative to the upper-left-hand corner
     * of the screen (again, in pixels))
     */
    public val viewportRect: Rect get() = rect.entitySpaceToViewportSpace(entity)

    /**
     * Check if this hitbox is currently colliding (overlapping) with a
     * given [other] hitbox.
     */
    fun isColliding(other: Hitbox): Boolean = this.worldRect in other.worldRect
}