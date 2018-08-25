package com.camerontauxe.futz.prefab

import com.camerontauxe.futz.*

import javafx.scene.canvas.GraphicsContext
import javafx.scene.text.Font
import javafx.scene.paint.Paint
import javafx.scene.paint.Color

/**
 * A type of entity that is represented by some text. Is visible, non-static and non-collidable.
 *
 * @property text A string of the text to display
 * @property font A JavaFX font used when drawing the text. Note that the font's size
 * will be interpreted as the height of a character in game-units.
 * Defaults to the system default font.
 * @property fill A JavaFX Paint object representing the color (or gradient) fill of the
 * text. Defaults to Color.BLACK
 */
public open class TextEntity(
    var text: String,
    var font: Font = Font.getDefault(),
    var fill: Paint = Color.BLACK
) : Entity() {

    public override var name = text

    public override fun draw(ctx: GraphicsContext) {
        ctx.setFill(fill);
        ctx.setFont(font)
        ctx.fillText(text, 0.0, 0.0)
    }
}