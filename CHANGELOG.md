# Version 0.2 Alpha
### New Features
* Added sprite sheet animation system
* Added sound effects and music system
### Changes
* Refactoring of asset managment: Removed Assets object, sprites can now be initialized without loading, meaning they can be initialized before calling FUTZ.inits
* FUTZ.currentScene is now publicly readable, but read-only
### Fixes
* Fixed GraphicsContext.transformEntitySpaceToViewport working incorrectly.