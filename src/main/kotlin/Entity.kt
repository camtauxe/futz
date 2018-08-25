package com.camerontauxe.futz

import javafx.scene.canvas.GraphicsContext

/**
 * Represents an object in the game world.
 * 
 * An entity is any object in the game world such as the player, an enemy, a pickup, a platform
 * or anything else. Every frame, each entity in the current scene has its [update] function
 * called which performs one frame of action for the entity (moving, responding to input,
 * collisions etc.). (This action for a specific kind of entity is defined by subclassing Entity
 * and overriding the [update] function.).
 *
 * After each entity in the scene has updated, every entities [draw] function is called. The draw
 * takes the passed JavaFX canvas context and renders the entity on the screen. Again, the way
 * a specific entity is drawn is defined by overriding the [draw] function.
 *
 * Adding or removing entities to the current scene is done through the entity's [spawn] and
 * [destroy] functions.
 */
public abstract class Entity() {

    /** Whether or not the entity is currently in the scene. (Read-Only) */
    var inScene = false
        internal set

    /** 
     * Whether or not an entity can be drawn. If this is 'false' then the
     * entity's [draw] function will never be called. This can be overriden
     * but remains constant for a specific sublcass of Entity
     * This is 'true' by default
     */
    public open val visible:       Boolean = true
    /**
     * Whether or not this entity can collide with other entities. If this is
     * 'false' then the entity will never be checked for collisions. This can
     * be overriden but remains constant for a specific subclass of entity.
     * This is 'false' by default
     */
    public open val collidable:    Boolean = false
    /**
     * Whether or not this entity shouldn't update each frame. If this is 'true' then
     * the entity's [update] function will never be called. This can be overriden
     * but remains contant for a specific subclass of Entity.
     * This is 'false' by default.
     */
    public open val static:        Boolean = false

    /**
     * A name describing the entity. It can be overriden, but remains constant
     * for a specific subclass of entity. In other words, it describes the class of
     * entity but not a specific instance.
     */
    public open val name:   String = "Unnamed Entity"
    /**
     * The hitbox essentially defines the "area" that an entity occupies. While the
     * [Hitbox] reference itself is a constant, the [Rect] it refers to can be changed.
     *
     * Hitboxes are used for collision detection, but even non-collidable entities may
     * wish to define a non-default hitbox because it is used in various functions that
     * consider the entity's "area" like [clampInside] and [centerInside]
     *
     * @see Hitbox
     * @see Rect
     */
    public open val hitbox: Hitbox = Hitbox(rect = Rect(0.0, 0.0, 0.1, 0.1), entity = this)

    /**
     * A vector2 describing this entity's position (in game-units) in the game world. 
     * Typically, this point represents the upper-left-hand corner of the entity.
     *
     * Note that setting the entity's position to another Vector2 copies the given Vector2 and
     * does not set the position as a reference to it.
     * Example:
     *
     *      newPosition: Vector2 = Vector2(3.0, 4.0)
     *      myEntity.position = newPosition
     *      println(myEntity.position)  // Prints "Vector2(X: 3.0, Y: 4.0)"
     *      newPosition.x = 7.0
     *      println(myEntity.position)  // Still prints "Vector2(X: 3.0, Y: 4.0)" because
     *                                  // newPosition was copied when it was assigned to
     *                                  // myEntity.position
     */
    public var position: MutableVector2 = MutableVector2(0.0, 0.0)
        set(value) {field = value.mutableCopy()}

    /** The entity's position (in pixels) relative to the current viewport. (Read-Only) */
    public val viewportPos: Vector2 get() = position.worldSpaceToViewport()

    /**
     * An entity's depth controls the order in which they are drawn. Lower values
     * are drawn first (and thus beneath entities with higher values).
     *
     * This is best set before an entity enters the scene. Setting it while the entity
     * is in the scene will cause the list of entities to be re-sorted by depth
     * which can potentially be slow if there are many active entities
     */
    public var depth = 0
        set(value) { field = value; if (inScene) Entities.sort() }

    /**
     * The list of entities that this entity is colliding with on this frame.
     * This is the mutable view of the list which is only visible internally
     */
    internal    val collidingMutable:   MutableList<Entity> = mutableListOf<Entity>()
    /** 
     * The list of entities that this entity is colliding with on this frame
     * This will always be empty for non-collidable entities
     */
    public      val collidingEntities:  List<Entity>        = collidingMutable

    /**
     * Add this entity into the scene. Optionally, at a provided position and depth.
     * 
     * Note that the entity will not be added immediately, but instead at the beginning
     * of the next frame. There is no need to call [start] yourself. It will be called
     * automatically after the entity is added to the scene.
     *
     * If a scene change is triggered on this frame, then the entity will not be
     * added (either to the current scene or the next one)
     * @see start
     */
    public fun spawn(atPos: Vector2? = null, atDepth: Int? = null) {
        depth = atDepth ?: depth
        position = atPos?.mutableCopy() ?: position
        Entities.add(this)
    }

    /**
     * Remove this entity from the scene.
     *
     * Note that the entity will not be removed immediately, but instead at the end of
     * the current frame. There is no need to call [cleanup] yourself. It will be called
     * automatically just before the entity is removed from the scene.
     * @see cleanup
     */
    public fun destroy() {
        Entities.remove(this)
    }

    /**
     * This will be called on the first frame that the entity is in the scene.
     * Override this to perform any intitialization that the entity requries before
     * it's first [update] call
     */
    public open fun start() {}
    /**
     * This will be called once per frame while the entity is in the scene (assuming the
     * entity is not [static]). Override this to perform one frame of action for the entity.
     * @see static
     */
    public open fun update() {}
    /**
     * This will be called once per frame while the entity is in the scene (assuming the
     * entity is [visible]). Override this to use the given graphicsContext to render the
     * entity onto the canvas. Note that when this is called, the graphicsContext will already
     * be transformed to draw relative to this entity's position (and scaled using game-units).
     * So for example, the code...
     *
     *      ctx.fillRect(Rect(x = 0.0, y = 0.0, width = 3.0, height = 4.0))
     *
     * ... in this function will draw a rectangle 3 game-units wide and 4 game-units tall
     * with the upper-left corner being exactly at the entity's current position.
     * @see visible
     */
    public open fun draw(ctx: GraphicsContext) {}
    /**
     * This will be called just before an entity is removed from a scene.
     * Override this to perform any cleanup or book-keeping necessary (if any).
     */
    public open fun cleanup() {}

    /**
     * Convienence function to check if this entity is currently colliding with
     * a given [other] entity on this frame. If this entity or the [other] entity
     * is non-collidable, this will always be 'false'
     */
    public fun isCollidingWith(other: Entity): Boolean = other in collidingEntities

    /**
     * Set the position of this entity so that it does not extend outside the
     * [bounds] specified by the given rectangle (in game-units). If the entity already fits
     * entirely within the [bounds], it will not be moved.
     * This makes use of the entity's hitbox even if the entity is non-collidable.
     * If the entity's hitbox is too large to fit inside the [bounds], it's position will
     * be set to the rectangle's x,y position, but the entity will not be resized in any way.
     * @see hitbox
     * @see Rect
     */
    public fun clampInside(bounds: Rect) {
        val rect = hitbox.worldRect
        if (rect.x < bounds.x)
            position.x = bounds.x
        else if (rect.x2 > bounds.x2)
            position.x = bounds.x2 - rect.width
        if (rect.y < bounds.y)
            position.y = bounds.y
        else if (rect.y2 > bounds.y2)
            position.y = bounds.y2 - rect.height
    }

    /**
     * Sets the position of this entity so that it lies in the center of
     * the [bounds] specified by the given rectangle (in game-units).
     * This makes use of the entity's hitbox even if the entity is non-collidable.
     * Note that his center's around the center of the entity's hitbox and not necessarily
     * the actual position of the entity.
     * @see hitbox
     * @see Rect
     */
    public fun centerInside(bounds: Rect) {
        val rect = hitbox.worldRect
        position.x = bounds.x + bounds.width / 2.0 - rect.width / 2.0
        position.y = bounds.y + bounds.height / 2.0 - rect.height /2.0
    }
}

// EXTENSION FUNCTIONS
// Resize various components to and from Entity Space (space relative to an entity)

// Transform GraphicsContext between Entity-Space and Viewport

/**
 * Transform the GraphicsContext (assuming it is currently transformed to the Viewport (in pixels))
 * to a space relative to the given [entity]. Such that drawing at position (0,0) draws at the
 * entities exact position and drawing a distance of 1 corresponds to one game-unit.
 * (This is the inverse of [GraphicsContext.transformEntitySpaceToViewport])
 * @see GraphicsContext.transformEntitySpaceToViewport
 * @see Viewport
 * @receiver GraphicsContext
 */
public fun GraphicsContext.transformViewportToEntitySpace(entity: Entity) {
    this.transformViewportToWorldSpace()
    this.transformWorldSpaceToEntitySpace(entity)
}

/**
 * Transform the GraphicsContext (assuming it is currently transformed relative to the given
 * [entity] such as when the entity's [draw][Entity.draw] function is called) to the viewport
 * (in pixels). Such that drawing at position (0,0) draws in the upper-left corner of the screen
 * and drawing a distance of 1 corresponds to one pixel
 * (This is the inverse of [GraphicsContext.transformViewportToEntitySpace])
 * @see GraphicsContext.transformViewportToEntitySpace
 * @see Viewport
 * @receiver GraphicsContext
 */
public fun GraphicsContext.transformEntitySpaceToViewport(entity: Entity) {
    this.transformEntitySpaceToWorldSpace(entity)
    this.transformWorldSpaceToViewport()
}

// Transform GraphicsContext between World-Space and Entity-Space

/**
 * Transform the GraphicsContext (assuming it is currently transformed to the world-space) to a
 * space relative to the given [entity]. Such that drawing at position (0,0) draws at the
 * world origin and drawing a distance of 1 corresponds to one game-unit.
 * (This is the inverse of [GraphicsContext.transformEntitySpaceToWorldSpace])
 * @see GraphicsContext.transformEntitySpaceToWorldSpace
 * @receiver GraphicsContext
 */
public fun GraphicsContext.transformWorldSpaceToEntitySpace(entity: Entity) {
    this.translate(entity.position.x, entity.position.y)
}

/**
 * Transform the GraphicsContext (assuming it is currently transformed relative to the given
 * [entity] such as when the entity's [draw][Entity.draw] function is called) to the world-space. 
 * Such that drawing at position (0,0) draws at the world origin and drawing a distance of 1
 * corresponds to one game-unit
 * (This is the inverse of [GraphicsContext.transformWorldSpaceToEntitySpace])
 * @see GraphicsContext.transformWorldSpaceToEntitySpace
 * @receiver GraphicsContext
 */
public fun GraphicsContext.transformEntitySpaceToWorldSpace(entity: Entity) {
    this.translate(-entity.position.x, -entity.position.y)
}

// Transform Vector2 between Viewport and Entity-Space

/**
 * Get a Vector2 representing this point (assuming this point is in game-units relative
 * to the given [entity]) as a position (in pixels) in the viewport. For example, if this
 * is called on a vector (0,0) for an entity, the returned vector2 will be the entity's
 * position in the viewport. Equivalent to [Entity.viewportPos].
 * (This is the inverse of [Vector2.viewportSpaceToEntitySpace])
 * @see Entity.viewportPos
 * @see Vector2.viewportSpaceToEntitySpace
 * @see Viewport
 * @receiver Vector2
 */
public fun Vector2.entitySpaceToViewportSpace(entity: Entity) = 
    this.entitySpaceToWorldSpace(entity).worldSpaceToViewport()
/**
 * Get a Vector2 representing this point (assuming this point is in pixels relative
 * to the viewport) as a position (in game-units) relative to the given [entity].
 * (This is the inverse of [Vector2.entitySpaceToViewportSpace])
 * @see Vector2.entitySpaceToViewportSpace
 * @see Viewport
 * @receiver Vector2
 */
public fun Vector2.viewportSpaceToEntitySpace(entity: Entity) =
    this.viewportToWorldSpace().worldSpaceToEntitySpace(entity)

// Transform Vector2 between World-Space and Entity-Space

/**
 * Get a Vector2 representing this (assuming this is relative to the given [entity]) point
 * in the world-space. For example: If an entity is at position (3,4) and this is called
 * on a vector (0,1) then the returned vector will be (3,5) (same x-coordinate and one game-unit
 * below the entity)
 * (This is the inverse of [Vector2.worldSpaceToEntitySpace])
 * @see Vector2.worldSpaceToEntitySpace
 * @receiver Vector2
 */
public fun Vector2.entitySpaceToWorldSpace(entity: Entity) = this + entity.position
/**
 * Get a Vector2 representing this point (in game-units) relative to the given [entity].
 * For example if an entiy is at position (3,4) and this is called on a vector (3,5) the
 * returned vector will be (0,1) same x-coordinate and one game-unit below the entity)
 * (This is the inverse of [Vector2.entitySpaceToWorldSpace])
 * @see Vector2.entitySpaceToWorldSpace
 * @receiver Vector2
 */
public fun Vector2.worldSpaceToEntitySpace(entity: Entity) = this - entity.position

// Transform Rect between Viewport and Entity-Space

/**
 * Get a rectangle representing this rectangle (relative to an [entity] in game-units) as a
 * rectangle on the viewport (in pixels).
 * (This is the inverse of [Rect.viewportSpaceToEntitySpace])
 * @see Rect.viewportSpaceToEntitySpace
 * @see Viewport
 * @receiver Rect
 */
public fun Rect.entitySpaceToViewportSpace(entity: Entity) =
    this.entitySpaceToWorldSpace(entity).worldSpaceToViewport()

/**
 * Get a rectangle representing this rectangle (assuming this rectangle is an area of the
 * viewport measured in pixels) as a rectangle in game-units relative to a given [entity]
 * (This is the inverse of [Rect.entitySpaceToViewportSpace])
 * @see Rect.entitySpaceToViewportSpace
 * @see Viewport
 * @receiver Rect
 */
public fun Rect.viewportSpaceToEntitySpace(entity: Entity) =
    this.viewportToWorldSpace().worldSpaceToEntitySpace(entity)

// Transform Rect between World-Space and Entity-Space

/**
 * Get a rectangle representing this rectangle (relative to an [entity] in game-units) as
 * a rectangle in world-space (in game-units)
 * (This is the inverse of [Rect.worldSpaceToEntitySpace])
 * @see Rect.worldSpaceToEntitySpace
 * @receiver Rect
 */
public fun Rect.entitySpaceToWorldSpace(entity: Entity) =
    Rect(
        position = this.position + entity.position,
        width = this.width,
        height = this.height
    )

/**
 * Get a rectangle representing this rectangle (in game-units) as a rectangle
 * relative to a given [entity].
 * (This is the inverse of [Rect.entitySpaceToWorldSpace])
 * @see Rect.entitySpaceToWorldSpace
 * @receiver Rect
 */
public fun Rect.worldSpaceToEntitySpace(entity: Entity) =
    Rect(
        position = this.position - entity.position,
        width = this.width,
        height = this.height
    )