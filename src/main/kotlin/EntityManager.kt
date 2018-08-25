package com.camerontauxe.futz

/**
 * Manages updating, adding and removing entities in the current scene.
 *
 * This singleton maintains a list of all of the entities currently in the scene and
 * manages updating and rendering of all of them each frame. Also manages adding and removing
 * entities from the scene. When entities are added or removed, the changes are buffered and made
 * at the beginning of the next frame.
 * This ensures that the list of active entities is constant throughout the entirety of a frame.
 */
public object Entities {

    /** This list of entities currently in the scene (mutable version is only visible internally) */
    internal val activeEntities  = mutableListOf<Entity>()
    /** Buffer of entities to be added at the beginning of the next frame */
    internal val addQueue        = mutableListOf<Entity>()
    /** Buffer of entities to be removed at the beginning of the next frame */
    internal val removeQueue     = mutableListOf<Entity>()

    /**
     * Whether or not the active entities need to be resorted (by depth) at the end of
     * the frame.
     * (This will be toggled if an entity changes its depth over the course of a frame)
     */
    private var shouldSort: Boolean = false

    /** The list of entities currently the scene (read-only)*/
    public val list: List<Entity> = activeEntities

    /**
     * Add the given [entity] to the scene. Note that the entity will not actually be added
     * until the beginning of the next frame. If the entity is already in the scene (or already
     * due to be added in the next frame), this does nothing.
     */
    public fun add(entity: Entity) {
        if (entity !in activeEntities && entity !in addQueue)
            addQueue.add(entity)
    }

    /**
     * Add multiple [entities] to the scene. Like with adding individual entities, the entities
     * will not actually be added until the beginning of the next frame.
     */
    public fun addAll(vararg entities: Entity) {
        for (e in entities) add(e)
    }

    /**
     * Remove the given [entity] from the scene. Note that the entity will not be removed
     * until the beginning of the next frame. If the given [entity] is not in the scene, 
     * this does nothing.
     */
    public fun remove(entity: Entity) {
        if (entity in addQueue)
            addQueue.remove(entity)
        else if (entity !in removeQueue)
            removeQueue.add(entity)
    }

    /**
     * Do one frame of action. Remove entities set to be removed, Add entities to be added,
     * and update all entities.
     */
    internal fun update() {

        // Call cleanup for all to-be-removed entities first.
        // (This is so that the cleanup function can safely assume that all other to-be-removed
        // entities will still exist in the scene when it is called)
        for (e in removeQueue) e.cleanup()
        // Remove all to-be-removed entities
        for (e in removeQueue) {
            activeEntities.remove(e)
            e.inScene = false
        }

        // Add all to-be-added entities to the scene
        var toInit = mutableListOf<Entity>();
        for (e in addQueue) {
            // Insert into the list so it is sorted by depth
            var i = activeEntities.indexOfLast({it.depth <= e.depth})
            if (i == activeEntities.size-1)
                activeEntities.add(e)
            else
                activeEntities.add(index = (if (i == -1) 0 else i), element = e)
            e.inScene = true
            toInit.add(e)
        }
        addQueue.clear()

        // Call 'start' for all entities.
        // (This is done after adding all entities so that the start function can safely assume
        // that all other entities that were added on the same frame are already in the scene
        // when it is called)
        for (e in toInit) e.start()

        // Calculate collisions for all collidable entities
        for (e in activeEntities.filter{it.collidable}) {
            e.collidingMutable.clear()
            for (e2 in activeEntities.filter({it.collidable && it != e})) {
                if (e.hitbox.isColliding(e2.hitbox) == true)
                    e.collidingMutable.add(e2)
            }
        }

        // Update all non-static entities
        for (e in activeEntities.filter{!it.static}) e.update()

        // If 'sort' was called at some point during the updating,
        // resort the entities by depth.
        if (shouldSort) {
            activeEntities.sortWith(EntityDepthComparator)
            shouldSort = false
        }

    }

    /**
     * Render all active entities
     */
    internal fun render() {
        val ctx = FUTZ.graphicsContext
        for (e in activeEntities.filter{it.visible}) {
            // graphicsContext is transformed to the space of each entity that's drawn
            ctx.save()
            ctx.transformViewportToEntitySpace(e)
            e.draw(ctx)
            ctx.restore()
        }
    }

    /**
     * Remove all entities from the scene. Note that this acts IMMEDIATELY
     * and thus shouldn't be called part-way through an update cycle. This also
     * cancels any entities that were going to be added in the next frame.
     */
    internal fun clearAll() {
        for (e in activeEntities) e.cleanup()
        activeEntities.clear()
        removeQueue.clear()
        addQueue.clear()
    }

    /**
     * Resort the list of active entities according to their depth. Note that the sort
     * will not occur until the end of the current frame.
     */
    internal fun sort() {
        shouldSort = true
    }
}

/**
 * A Comparator object to compare the depth of two entities
 */
private object EntityDepthComparator : Comparator<Entity> {
    override fun compare(a: Entity, b: Entity): Int = a.depth - b.depth
}