package com.camerontauxe.futz

import javafx.scene.canvas.GraphicsContext

/**
 * Represents a Two-dimensional vector with x and y coordinates, such as a position or speed.
 * Normally immutable, however there is the [MutableVector2] subclass which will allow its
 * values to be changed.
 * @property x The x-coordinate of the vector
 * @property y The y-coordinate of the vector
 * @see MutableVector2
 */
public open class Vector2(open val x: Double, open val y: Double) {
    /** Get a copy of this Vector2 */
    public fun copy():         Vector2         = Vector2(x,y)
    /** Get a copy of this Vector2 that is mutable */
    public fun mutableCopy():  MutableVector2  = MutableVector2(x,y)

    /** Component operator function (for destructuring): Returns [x] */
    public operator fun component1(): Double = x
    /** Component operator function (for destructuring): Returns [y] */
    public operator fun component2(): Double = y

    /** Addition operator: Result = Vector2(x = a.x + b.x, y = a.y + b.y) */
    public open operator fun plus(other: Vector2) = Vector2(this.x + other.x, this.y + other.y)
    /** Subtraction operator: Result = Vector2(x = a.x - b.x, y = a.y - b.y) */
    public open operator fun minus(other: Vector2) = Vector2(this.x - other.x, this.y - other.y)

    // @TODO: Multiplication and division operators (with other vectors and with scalars)

    /** Check if the point represented by this Vector2 resides inside the given [rect] */
    public fun isInside(rect: Rect) = this in rect

    /** String representation in of this Vector 2 in the form "Vector2(X: [x] Y: [y])" */
    public override fun toString(): String = "Vector2(X: $x\tY: $y )"
}

/**
 * Subclass of Vector2 which allows its values to be changed.
 * @see Vector2
 */
public class MutableVector2(x: Double, y: Double) : Vector2(x,y) {
    override var x = x
    override var y = y

    // Values returned from adding or subtracting on mutable vectors are mutable as well
    override operator fun plus(other: Vector2) = MutableVector2(this.x + other.x, this.y + other.y)
    override operator fun minus(other: Vector2) = MutableVector2(this.x - other.x, this.y - other.y)
}

/**
 * Represents a rectangular area in space. The [x] and [y] properties represent the position of
 * the upper-left-hand corner of the rectangle. Normally immutable, however there is the
 * [MutableRect] subclass which will allow its values to be changed.
 * @property x The x-coordinate of the upper-left-hand corner of the the rectangle
 * @property y The y-coordinate of the upper-left-hand corner of the the rectnangle
 * @property width The width of the rectangle
 * @property height The height of the rectangle
 * @see MutableRect
 */
open class Rect(
    open val x: Double,
    open val y: Double,
    open val width: Double,
    open val height: Double
) {
    /** X-coordinate of the right-hand side of the rectangle (read-only) */
    public val x2:          Double  get() = x + width
    /** Y-coordinate of the bottom-side of the rectangle (read-only) */
    public val y2:          Double  get() = y + height
    /** The upper-left-hand corner of this rectangle as a Vector2 */
    public val position:    Vector2 get() = Vector2(x, y)

    /**
     * Alternate constructor which takes the position of the upper-left-hand
     * corner as a Vector2
     */
    constructor(
        position: Vector2,
        width: Double,
        height: Double
    ) : this(position.x, position.y, width, height)

    /** Get a copy of this rectangle */
    public fun copy():         Rect        = Rect(x,y,width,height)
    /** Get a copy of this rectangle that is mutable */
    public fun mutableCopy():  MutableRect = MutableRect(x,y,width,height)

    /**
     * Overload 'in' operator for other rectangles to check if the two rectangles
     * overlap at any point.
     */
    public operator fun contains(other: Rect): Boolean =
        this.x <= other.x2 && this.x2 >= other.x &&
        this.y <= other.y2 && this.y2 >= other.y

    /**
     * Check if this rectangle and a given [other] rectangle overlap at any point.
     * This can also be done with the 'in' operator 
     */
    public fun overlapping(other: Rect) = contains(other)

    /**
     * Overload 'in' operator for Vector2's to check if the point represented
     * by the Vector2 lies inside this rectangle.
     */
    public operator fun contains(point: Vector2): Boolean =
        point.x >= this.x && point.x <= this.x2 &&
        point.y >= this.y && point.y <= this.y2

    /** 
     * String represetnation of the this rectnalge in the form:
     * "Rect(X: [x] Y: [y] Dimensions: [width] X [height]"
     */
    public override fun toString(): String = "Rect(X: $x\tY: $y\tDimensions: $width X $height )"
}

/**
 * Subclass of Rect which allows its values to be changed.
 * @see Rect
 */
public class MutableRect(
    x: Double,
    y: Double,
    width: Double,
    height: Double
) : Rect(x,y,width,height) {
    override var x = x
    override var y = y
    override var width = width
    override var height = height
}

// Extension functions

/**
 * Fill the given rectangle with whatever this context's current fill is.
 * @receiver GraphicsContext
 */
public fun GraphicsContext.fillRect(rect: Rect) {
    this.fillRect(rect.x, rect.y, rect.width, rect.height)
}

/**
 * Stroke the given rectangle with whatever this context's current stroke is.
 * @receiver GraphicsContext
 */
public fun GraphicsContext.strokeRect(rect: Rect) {
    this.strokeRect(rect.x, rect.y, rect.width, rect.height)
}