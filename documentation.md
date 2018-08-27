# Module Futz

## FUTZ Game Library
## Current Version: 0.1 Alpha

Futz is a library for the creation of simple, 2D games in Kotlin using JavaFX. A Futz game works using [entities][com.camerontauxe.futz.Entity], which represent game objects existing, moving and interacting within the game-world. Every frame (roughly 60 times per second), all of the entities in the world have a chance to update and afterwards the game-world is rendered onto a JavaFX canvas. Currently, Futz provides utilities for dealing with the JavaFX backend to get a game up-and-running, managing entities (spawning, updating, destroying, colliding, rendering), handling input, debugging and loading text and image assets. But more functionality is on the way.

To start, take a look at the documentation for some of the most important classes:
* [FUTZ][com.camerontauxe.futz.FUTZ] The "core" of the engine. Manages all other parts of the engine and contains anything pertaining to the global state  of Futz. Critically contains the function [init][com.camerontauxe.futz.FUTZ.init], which the user calls to initialize everything.
* [Entity][com.camerontauxe.futz.Entity] The entity base class. All entities in your game will derive from this.
* [Input][com.camerontauxe.futz.Input] The Futz input manager. Can be queried for the current state of keyboard or mouse input (updates every frame).
* [Scene][com.camerontauxe.futz.Scene] (Not to be confused with a JavaFX Scene), defines a single "area" of the game (such as a level or menu screen) as a routine for setting up and spawning entities.
* [Assets][com.camerontauxe.futz.Assets] Everything pertaining to loading and managing game assets.
* [Camera][com.camerontauxe.futz.Camera] and [Viewport][com.camerontauxe.futz.Viewport] Everything pertaining to the size and position of the camera within the game world and relating distances in game-units to pixels on the screen. Understanding this is critical to being able to rendering more advanced entities.

# Package com.camerontauxe.futz

This package contains all of the core features and functionality of Futz

# Package com.camerontauxe.futz.prefab

This package contains a few pre-made entities or scenes that may be useful.