# FUTZ Game Library
## Version: 0.2 ALPHA

## Full Documentation: [Click Here](http://futz.camerontauxe.com/doc/futz/index.html)

### What is Futz?

Futz is a Java/Kotlin game-making library designed to make it easy for absolute beginners to create simple, 2D games. The idea came about when I was trying to teach some of my non-programmer friends the basics. I figured they would have more fun learning if they could work on a real project instead of just endless exercises. And so I began creating Futz with the goal of getting some of the more confusing house-keeping involved in making a game out of the way and allowing my friends to jump right in to making a game and learning basic programming concepts (object-oriented design, functions, variables, loops, etc) in a fun environment.

### Why so incomplete?

Right now, Futz is extremely un-finished. It's rough around the edges in many places and many crucial features are just..not..there. The only reason right now that I am releasing this publicly right now is that I am planning on taking Futz on its first major test-drive during the [2018 Game Maker's Toolkit Game jam](https://itch.io/jam/gmtk-2018). It would be against the rules for me to create my game using a bunch of private code I wrote before the jam started. I am releasing Futz now so that *technically* anyone else can start at the same point I am (though in reality most people will probably starting far ahead of me by virtue of using an *actual* game engine).

### Current features

* Start JavaFX and get a game running with just a single function call to **FUTZ.init**
* Entity Management (spawn, destroy, update, render, etc.)
* Input Management
* Box collision detection
* Built-in basic entities for drawing sprites, rectangles, or text
* Lots of small, helpful abstractions (nothing ground-breaking, but it'll save you some time in a game jam)

### Planned features

I plan to keep adding functionality to Futz as I encounter the need for features. Though it is important to me to maintain the bottom-line that Futz be easy for beginners to work with, so any features I add should be very simple to use, or possible to completely ignore in the case of more advanced functionality. That said, there are a number of features that are immediately on the drawing board:

* Sound
* Scaling and rotating of entities
* Entity heirarchies (i.e. entities can be the child or parent of another entity)

### Build/Installation

Futz is built with Maven, so you will need to download and install it before you can work with it. Once that is done...

1. Clone the repository
2. Navigate to the root of the repository (where **pom.xml** is) and install with maven:

```
mvn install
```
3. This will add Futz to the your Maven installation's local repository. To use it, create another Maven project somewhere and simply add Futz as a dependency.

### How to use Futz

For a blank template project, go [HERE](https://github.com/camtauxe/futz-blank-project).

A Futz game can be started with a call to the **FUTZ.init** function. The function takes a number of arguments that can all be left out (given default values) if you desire. **BUT** you *must* override either **startCallback** or **initialScene** in order to be able to do anything with Futz.

Overriding **startCallback** is the recommending route for beginners (this is what's done in the template project). It simply allows you to specify a function to be called once Futz has started. In this function, you can begin to initialize and spawn entities, load assets, change the scene, etc, all in the context of a default scene created by Futz.

You can also instead override **initialScene** and provide your own Futz scene (not to be confused with a JavaFX Scene) which Futz will use as the first scene after the splash screen. This will allow you a little more control over what happens in the scene than just using the default scene. See the documentation for Scenes in the source code [HERE](src/main/kotlin/Scene.kt)
  
For more details see the full documentation [HERE](http://futz.camerontauxe.com/doc/futz/index.html)
