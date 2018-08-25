package com.camerontauxe.futz

/**
 * A scene in FUTZ represents a single "area" in the game such as a level, menu screen or hub world.
 *
 * Different scenes are created by subclassing Scene and overriding the various functions in it.
 * Most important is the [construct] function which is called whenever the FUTZ switches
 * to the scene and should initialize and spawn all entities needed for the scene.
 *
 * A scene can also be named (usually for debugging purposes) and contains [update]
 * and [render] functions which can be used to update or draw things pertaining to the global
 * state of the scene independent of any particular entities in the scene.
 *
 * @see Scene.construct
 */
public abstract class Scene() {

    /** The name of the scene (Usually used for debugging) */
    public open val name = "Untitled Scene"
    /**
     * This will be called when FUTZ switches to this scene. It should initialize and
     * spawn whatever entities are needed for the scene when it starts.
     */
    public open fun construct() {}
    /**
     * This will be called every frame while this is the current scene. Note that this is called
     * BEFORE any of the entities in the scene are updated for this frame.
     */
    public open fun update() {}
    /**
     * This will be called every frame while this is the current scene. Note that this is called
     * AFTER all of the entities in the scene have been updated and rendered. At the time this is
     * called [FUTZ.graphicsContext] will be scaled to the viewport (where 0,0 refers to the
     * upper-left-hand corner of the viewport and distances drawn are in pixels)
     */
    public open fun render() {}
    /**
     * This will be called when FUTZ switches away from this scene. It should perform any
     * cleanup necessary before leaving the scene. It is NOT necessary to remove all the entities
     * from the scene here as they will be automatically removed just as the scene changes.
     */
    public open fun destroy() {}
}

/**
 * A blank scene with nothing in it that does nothing. If no initial scene is specified
 * when starting FUTZ, this will be switched to after the splash screen.
 */
public object EmptyScene : Scene() {
    override val name = "Default Scene"
}