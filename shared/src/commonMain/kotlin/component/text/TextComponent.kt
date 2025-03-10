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

import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.unit.toRect
import com.patrykandpatrick.vico.core.DEF_LABEL_LINE_COUNT
import com.patrykandpatrick.vico.core.DefaultDimens.TEXT_COMPONENT_TEXT_SIZE
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.dimension.Margins
import com.patrykandpatrick.vico.core.component.dimension.Padding
import com.patrykandpatrick.vico.core.context.Extras
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.context.getOrPutExtra
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.piRad
import extension.isLtr
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
        extras: Extras,
        text: CharSequence,
        textX: Float,
        textY: Float,
        horizontalPosition: Alignment.Horizontal = Alignment.CenterHorizontally,
        verticalPosition: Alignment.Vertical = Alignment.CenterVertically,
        maxTextWidth: Int = Int.MAX_VALUE,
        maxTextHeight: Int = Int.MAX_VALUE,
        rotationDegrees: Float = 0f,
    ): Unit = with(drawScope) {
        if (text.isBlank()) return
        layout = getLayout(
            extras = extras,
            text = text,
            fontScale = fontScale,
            width = maxTextWidth,
            height = maxTextHeight,
            rotationDegrees = rotationDegrees,
        )

        val shouldRotate = rotationDegrees % 2f.piRad != 0f
        val textStartPosition =
            horizontalPosition.getTextStartPosition(drawScope, textX, layout!!.size.width.toFloat())
        val textTopPosition =
            verticalPosition.getTextTopPosition(drawScope, textY, layout!!.size.height.toFloat())

        with(drawContext.canvas) {
            save()

            var bounds = layout!!.size
            val textAlignCorrection =
                horizontalPosition.getXCorrection(width = bounds.width.toFloat())

            val rect = bounds.toIntRect()
            bounds = rect.copy(
                left = rect.left - padding.getLeftDp(isLtr).toPx().toInt(),
                top = rect.top - padding.top.toPx().toInt(),
                right = rect.right + padding.getRightDp(isLtr).toPx().toInt(),
                bottom = rect.bottom + padding.bottom.toPx().toInt()
            ).size

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

            bounds = bounds.toIntRect().toRect().translate(
                textStartPosition + xCorrection,
                textTopPosition + yCorrection,
            ).roundToIntRect().size

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
            drawScope.drawText(
                textMeasurer!!,
                text.toString(),
                Offset(textStartPosition, textTopPosition)
            )
            restore()
        }
    }

    private fun Alignment.Horizontal.getTextStartPosition(
        drawScope: DrawScope,
        baseXPosition: Float,
        width: Float,
    ): Float = with(drawScope) {
        when (this@getTextStartPosition) {
            Alignment.Start ->
                if (isLtr) getTextRightPosition(baseXPosition, width) else getTextLeftPosition(
                    baseXPosition
                )

            Alignment.Center ->
                baseXPosition - width.half

            Alignment.End ->
                if (isLtr) getTextLeftPosition(baseXPosition) else getTextRightPosition(
                    baseXPosition,
                    width
                )

            else -> error("Not Supported")
        }
    }

    private fun DrawScope.getTextLeftPosition(baseXPosition: Float): Float =
        baseXPosition + padding.getLeftDp(isLtr).toPx() + margins.getLeftDp(isLtr).toPx()

    private fun DrawScope.getTextRightPosition(
        baseXPosition: Float,
        width: Float,
    ): Float =
        baseXPosition - padding.getRightDp(isLtr).toPx() - margins.getRightDp(isLtr).toPx() - width

    private fun Alignment.Horizontal.getXCorrection(width: Float): Float = when (this) {
        Alignment.Start -> 0f
        Alignment.CenterHorizontally -> width.half
        Alignment.End -> width
        else -> error("")
    }

    @JvmName("getTextTopPositionExt")
    private fun Alignment.Vertical.getTextTopPosition(
        density: Density,
        textY: Float,
        layoutHeight: Float,
    ): Float = with(density) {
        textY + when (this@getTextTopPosition) {
            Alignment.Top -> -layoutHeight - padding.bottom.toPx() - margins.bottom.toPx()
            Alignment.CenterVertically, Alignment.Center -> -layoutHeight.half
            Alignment.Bottom -> padding.top.toPx() + margins.top.toPx()
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
        extras = context,
        density = Density(context.density),
        text = text,
        width = width,
        height = height,
        rotationDegrees = rotationDegrees,
    ).width

    /**
     * Returns the height of this [TextComponent] for the given [text] and the available [width] and [height].
     */
    public fun getHeight(
        extras: Extras,
        density: Density,
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
        height: Int = Int.MAX_VALUE,
        rotationDegrees: Float = 0f,
    ): Float = getTextBounds(
        extras = extras,
        density = density,
        text = text,
        width = width,
        height = height,
        rotationDegrees = rotationDegrees,
    ).height

    /**
     * Returns the bounds ([RectF]) of this [TextComponent] for the given [text] and the available [width] and [height].
     */
    public fun getTextBounds(
        extras: Extras,
        density: Density,
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
        height: Int = Int.MAX_VALUE,
        outRect: Rect = tempMeasureBounds,
        includePaddingAndMargins: Boolean = true,
        rotationDegrees: Float = 0f,
    ): Rect = with(density) {
        getLayout(
            extras = extras,
            text = text,
            fontScale = fontScale,
            width = width,
            height = height,
            rotationDegrees = rotationDegrees,
        ).size.toIntRect().toRect().run {
            //  .getBounds(outRect).apply {
            if (includePaddingAndMargins) {
                copy(
                    right = this.right + padding.horizontal.toPx(),
                    bottom = this.bottom + padding.vertical.toPx(),
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
    private fun Density.getLayout(
        extras: Extras,
        text: CharSequence,
        fontScale: Float,
        width: Int = Int.MAX_VALUE,
        height: Int = Int.MAX_VALUE,
        rotationDegrees: Float = 0f,
    ): TextLayoutResult {
        val widthWithoutMargins = width - (margins as MutableDimensions).horizontal.toPx().toInt()
        val heightWithoutMargins = height - (margins as MutableDimensions).vertical.toPx().toInt()

        val correctedWidth = (
                when {
                    rotationDegrees % 1f.piRad == 0f -> widthWithoutMargins
                    rotationDegrees % .5f.piRad == 0f -> heightWithoutMargins
                    else -> {
                        val cumulatedHeight = lineCount + padding.vertical.toPx().toInt()
                        val alpha = 0f // Math.toRadians(rotationDegrees.toDouble())
                        val absSinAlpha = sin(alpha).absoluteValue
                        val absCosAlpha = cos(alpha).absoluteValue
                        val basedOnWidth =
                            (widthWithoutMargins - cumulatedHeight * absSinAlpha) / absCosAlpha
                        val basedOnHeight =
                            (heightWithoutMargins - cumulatedHeight * absCosAlpha) / absSinAlpha
                        min(basedOnWidth, basedOnHeight).toInt()
                    }
                } - padding.horizontal.toPx().toInt()
                ).coerceAtLeast(0)

        val key = LAYOUT_KEY_PREFIX + text + correctedWidth + rotationDegrees + textPaint.hashCode()
        return extras.getOrPutExtra(key = key) {
//            textPaint.textSize = textSizeSp * fontScale
            textMeasurer!!.measure(
                text = text.toString(),
                maxLines = lineCount,
            )
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
            margins = this@Builder.margins
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
