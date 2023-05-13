/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.core.component.text

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.unit.toRect
import com.patrykandpatrick.vico.core.DEF_LABEL_LINE_COUNT
import com.patrykandpatrick.vico.core.DefaultDimens.TEXT_COMPONENT_TEXT_SIZE
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.dimension.Margins
import com.patrykandpatrick.vico.core.component.dimension.Padding
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.context.getOrPutExtra
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.piRad
import kotlin.jvm.JvmName
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val TEXT_MEASUREMENT_CHAR = ""
private const val LAYOUT_KEY_PREFIX = "layout_"

/**
 * Uses [Canvas] to render text. This class utilizes [StaticLayout] and supports the following:
 *
 * - multi-line text with automatic line breaking
 * - text truncation
 * - [Spanned]
 * - text rotation
 * - text backgrounds (any [Component])
 * - margins and padding
 *
 * @see [textComponent]
 */
public open class TextComponent protected constructor() : Padding, Margins {

    @OptIn(ExperimentalTextApi::class)
    private var textMeasurer: TextMeasurer? = null

    private val textPaint: Paint = Paint().apply { isAntiAlias = true }
    private var tempMeasureBounds = Rect.Zero

    /**
     * The text’s color.
     */
    public var color: Color by textPaint::color

    /**
     * The [Typeface] for the text.
     */
//    public var typeface: Typeface? by textPaint::typeface

    /**
     * The font size (in sp).
     */
    public var textSizeSp: Float = 0f

    /**
     * The type of text truncation to be used when the text’s width exceeds the amount of available space. By default,
     * text is truncated at the end, and an ellipsis (…) is used.
     */
//    public var ellipsize: TextUtils.TruncateAt? = TextUtils.TruncateAt.END

    /**
     * The maximum number of lines for the text. For performance reasons, during the measurement phase, it is presumed
     * that the actual number of lines is equal to this value.
     */
    public var lineCount: Int = DEF_LABEL_LINE_COUNT

    /**
     * The text’s background. Use [padding] to set the padding between the text and the background.
     *
     * @see [padding]
     */
    public var background: Component? = null

    /**
     * The text alignment.
     */
//    public var textAlign: Paint.Align by textPaint::textAlign

    /**
     * The padding between the text and the background. This is applied even if [background] is null.
     *
     * @see [background]
     */
    override var padding: MutableDimensions = emptyDimensions()

    /**
     * The margins around the background. This is applied even if [background] is null.
     *
     * @see [background]
     */
    override var margins: MutableDimensions = emptyDimensions()

    private var layout: TextLayoutResult? = null

    /**
     * Uses [Canvas] to draw this [TextComponent].
     *
     * @param context holds environment data.
     * @param text the text to be drawn.
     * @param textX the _x_ coordinate for the text.
     * @param textY the _y_ coordinate for the text.
     * @param horizontalPosition the horizontal position of the text, relative to [textX].
     * @param verticalPosition the vertical position of the text, relative to [textY].
     * @param maxTextWidth the maximum width available for the text (in pixels).
     * @param maxTextHeight the maximum height available for the text (in pixels).
     * @param rotationDegrees the rotation of the text (in degrees).
     */
    @OptIn(ExperimentalTextApi::class)
    public fun drawText(
        drawScope: DrawScope,
        context: DrawContext,
        text: CharSequence,
        textX: Float,
        textY: Float,
        horizontalPosition: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalPosition: Alignment.Vertical = Alignment.CenterVertically,
        maxTextWidth: Int = Int.MAX_VALUE,
        maxTextHeight: Int = Int.MAX_VALUE,
        rotationDegrees: Float = 0f,
    ): Unit = with(context) {
        if (text.isBlank()) return
        layout = getLayout(
            text = text,
            fontScale = fontScale,
            width = maxTextWidth,
            height = maxTextHeight,
            rotationDegrees = rotationDegrees,
        )

        val shouldRotate = rotationDegrees % 2f.piRad != 0f
//        val textStartPosition = horizontalPosition.getTextStartPosition(context, textX, 0f) // layout.widestLineWidth)
//        val textTopPosition = verticalPosition.getTextTopPosition(context, textY, 0f) // layout.height.toFloat())

        with(context.canvas) {
            save()

//            val bounds = layout.getBounds(tempMeasureBounds)
//            val textAlignCorrection = textAlign.getXCorrection(width = bounds.width())

//            with(receiver = bounds) {
//                left -= padding.getLeftDp(isLtr).pixels
//                top -= padding.topDp.pixels
//                right += padding.getRightDp(isLtr).pixels
//                bottom += padding.bottomDp.pixels
//            }

            var xCorrection = 0f
            var yCorrection = 0f

            if (shouldRotate) {
//                val boundsPostRotation = bounds.copy().rotate(rotationDegrees)
//                val heightDelta = bounds.height() - boundsPostRotation.height()
//                val widthDelta = bounds.width() - boundsPostRotation.width()

//                xCorrection = when (horizontalPosition) {
//                    HorizontalPosition.Start -> widthDelta.half
//                    HorizontalPosition.End -> -widthDelta.half
//                    else -> 0f
//                } * context.layoutDirectionMultiplier

//                yCorrection = when (verticalPosition) {
//                    VerticalPosition.Top -> heightDelta.half
//                    VerticalPosition.Bottom -> -heightDelta.half
//                    else -> 0f
//                }
            }

//            bounds.translate(
//                x = textStartPosition + xCorrection,
//                y = textTopPosition + yCorrection,
//            )

//            if (shouldRotate) {
//                rotate(rotationDegrees, bounds.center.x, bounds.centerY())
//            }
//
//            background?.draw(
//                context = context,
//                left = bounds.left,
//                top = bounds.top,
//                right = bounds.right,
//                bottom = bounds.bottom,
//            )

//            translate(
//                bounds.left + padding.getLeftDp(isLtr).pixels + textAlignCorrection,
//                bounds.top + padding.topDp.pixels,
//            )
//
//            layout.draw(this)
            drawScope.drawText(textMeasurer!!, text.toString(), Offset(textX, textY))
            restore()
        }
    }

    private fun Alignment.Horizontal.getTextStartPosition(
        context: MeasureContext,
        baseXPosition: Float,
        width: Float,
    ): Float = with(context) {
        when (this@getTextStartPosition) {
            Alignment.Start ->
                if (isLtr) getTextRightPosition(baseXPosition, width) else getTextLeftPosition(baseXPosition)
            Alignment.Center ->
                baseXPosition - width.half
            Alignment.End ->
                if (isLtr) getTextLeftPosition(baseXPosition) else getTextRightPosition(baseXPosition, width)
            else -> error("Not Supported")
        }
    }

    private fun MeasureContext.getTextLeftPosition(baseXPosition: Float): Float =
        baseXPosition + padding.getLeftDp(isLtr).pixels + margins.getLeftDp(isLtr).pixels

    private fun MeasureContext.getTextRightPosition(
        baseXPosition: Float,
        width: Float,
    ): Float = baseXPosition - padding.getRightDp(isLtr).pixels - margins.getRightDp(isLtr).pixels - width

//    private fun Paint.Align.getXCorrection(width: Float): Float = when (this) {
//        Paint.Align.LEFT -> 0f
//        Paint.Align.CENTER -> width.half
//        Paint.Align.RIGHT -> width
//    }

    @JvmName("getTextTopPositionExt")
    private fun Alignment.Vertical.getTextTopPosition(
        context: MeasureContext,
        textY: Float,
        layoutHeight: Float,
    ): Float = with(context) {
        textY + when (this@getTextTopPosition) {
            Alignment.Top -> -layoutHeight - padding.bottomDp.pixels - margins.bottomDp.pixels
            Alignment.Center -> -layoutHeight.half
            Alignment.Bottom -> padding.topDp.pixels + margins.topDp.pixels
            else -> error("")
        }
    }

    /**
     * Returns the width of this [TextComponent] for the given [text] and the available [width] and [height].
     */
    public fun getWidth(
        context: MeasureContext,
        text: CharSequence,
        width: Int = Int.MAX_VALUE,
        height: Int = Int.MAX_VALUE,
        rotationDegrees: Float = 0f,
    ): Float = getTextBounds(
        context = context,
        text = text,
        width = width,
        height = height,
        rotationDegrees = rotationDegrees,
    ).width

    /**
     * Returns the height of this [TextComponent] for the given [text] and the available [width] and [height].
     */
    public fun getHeight(
        context: MeasureContext,
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
        height: Int = Int.MAX_VALUE,
        rotationDegrees: Float = 0f,
    ): Float = getTextBounds(
        context = context,
        text = text,
        width = width,
        height = height,
        rotationDegrees = rotationDegrees,
    ).height

    /**
     * Returns the bounds ([RectF]) of this [TextComponent] for the given [text] and the available [width] and [height].
     */
    public fun getTextBounds(
        context: MeasureContext,
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
        height: Int = Int.MAX_VALUE,
        outRect: Rect = tempMeasureBounds,
        includePaddingAndMargins: Boolean = true,
        rotationDegrees: Float = 0f,
    ): Rect = with(context) {
        getLayout(
            text = text,
            fontScale = fontScale,
            width = width,
            height = height,
            rotationDegrees = rotationDegrees,
        ).size.toIntRect().toRect().run {
            //  .getBounds(outRect).apply {
            if (includePaddingAndMargins) {
                copy(
                    right = this.right + padding.horizontalDp.pixels,
                    bottom = this.bottom + padding.verticalDp.pixels,
                )
            } else this
        }
//            .rotate(rotationDegrees).apply {
//            if (includePaddingAndMargins) {
//                right += margins.horizontalDp.pixels
//                bottom += margins.verticalDp.pixels
//            }
        }

    @OptIn(ExperimentalTextApi::class)
    private fun MeasureContext.getLayout(
        text: CharSequence,
        fontScale: Float,
        width: Int = Int.MAX_VALUE,
        height: Int = Int.MAX_VALUE,
        rotationDegrees: Float = 0f,
    ): TextLayoutResult {
        val widthWithoutMargins = width - margins.horizontalDp.wholePixels
        val heightWithoutMargins = height - margins.verticalDp.wholePixels

        val correctedWidth = (
            when {
                rotationDegrees % 1f.piRad == 0f -> widthWithoutMargins
                rotationDegrees % .5f.piRad == 0f -> heightWithoutMargins
                else -> {
                    val cumulatedHeight = lineCount + padding.verticalDp.wholePixels
                    val alpha = 0f // Math.toRadians(rotationDegrees.toDouble())
                    val absSinAlpha = sin(alpha).absoluteValue
                    val absCosAlpha = cos(alpha).absoluteValue
                    val basedOnWidth = (widthWithoutMargins - cumulatedHeight * absSinAlpha) / absCosAlpha
                    val basedOnHeight = (heightWithoutMargins - cumulatedHeight * absCosAlpha) / absSinAlpha
                    min(basedOnWidth, basedOnHeight).toInt()
                }
            } - padding.horizontalDp.wholePixels
            ).coerceAtLeast(0)

        val key = LAYOUT_KEY_PREFIX + text + correctedWidth + rotationDegrees + textPaint.hashCode()
        return getOrPutExtra(key = key) {
//            textPaint.textSize = textSizeSp * fontScale
            textMeasurer!!.measure(
                text = text.toString(),
                maxLines = lineCount,
            )
//            staticLayout(
//                source = text,
//                paint = textPaint,
//                width = correctedWidth,
//                maxLines = lineCount,
//                ellipsize = ellipsize,
//            )
        }
    }

    /**
     * The builder for [TextComponent].
     * @see textComponent
     */
    public class Builder {

        public var drawScope: DrawScope? = null

        @OptIn(ExperimentalTextApi::class)
        public var textMeasurer: TextMeasurer? = null

        /**
         * @see [TextComponent.color]
         */
        public var color: Int = Color.Black.toArgb()

        /**
         * @see [TextComponent.textSizeSp]
         */
        public var textSizeSp: Float = TEXT_COMPONENT_TEXT_SIZE

        /**
         * @see [TextComponent.typeface]
         */
        public var typeface: Typeface? = null

        /**
         * @see [TextComponent.ellipsize]
         */
//        public var ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END

        /**
         * @see [TextComponent.lineCount]
         */
        public var lineCount: Int = DEF_LABEL_LINE_COUNT

        /**
         * @see [TextComponent.background]
         */
        public var background: Component? = null

        /**
         * @see [TextComponent.textAlign]
         */
//        public var textAlign: Paint.Align = Paint.Align.LEFT

        /**
         * @see [TextComponent.padding]
         */
        public var padding: MutableDimensions = emptyDimensions()

        /**
         * @see [TextComponent.margins]
         */
        public var margins: MutableDimensions = emptyDimensions()

        /**
         * Creates a new instance of [TextComponent] with the supplied properties.
         */
        @OptIn(ExperimentalTextApi::class)
        public fun build(): TextComponent = TextComponent().apply {
            color = Color(this@Builder.color)
            textSizeSp = this@Builder.textSizeSp
            typeface = this@Builder.typeface
//            ellipsize = this@Builder.ellipsize
            lineCount = this@Builder.lineCount
            background = this@Builder.background
//            textAlign = this@Builder.textAlign
            padding.set(this@Builder.padding)
            margins.set(this@Builder.margins)
            textMeasurer = this@Builder.textMeasurer
        }
    }
}

/**
 * The builder DSL for [TextComponent].
 *
 * Example usage:
 * ```
 * textComponent {
 *    this.color = 0xFF000000 // This corresponds to #000000, which is black.
 *    this.textSizeSp = 12f
 *    this.typeface = Typeface.MONOSPACE
 * }
 *```
 */
public inline fun textComponent(block: TextComponent.Builder.() -> Unit = {}): TextComponent =
    TextComponent.Builder().apply(block).build()
