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

package com.patrykandpatrick.vico.core.component.shape

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.DEF_MARKER_TICK_SIZE
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction
import component.shape.MarkerCorneredShape
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.dimensions.Dimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions

/**
 * Adds a rounded rectangle to the receiver [Path].
 *
 * @param left the _x_ coordinate of the left edge of the rectangle.
 * @param top the _y_ coordinate of the top edge of the rectangle.
 * @param right the _x_ coordinate of the right edge of the rectangle.
 * @param bottom the _y_ coordinate of the bottom edge of the rectangle.
 * @param rect the rounded rectangle to be drawn.
 * @param radii used to store the corner radii. This array must be mutable.
 */
@Suppress("MagicNumber")
@LongParameterListDrawFunction
public fun Path.addRoundRect(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    rect: RoundRect,
    radii: FloatArray,
) {
    radii[0] = rect.topLeftCornerRadius.x
    radii[1] = rect.topLeftCornerRadius.y
    radii[2] = rect.topRightCornerRadius.x
    radii[3] = rect.topRightCornerRadius.y
    radii[4] = rect.bottomRightCornerRadius.x
    radii[5] = rect.bottomRightCornerRadius.y
    radii[6] = rect.bottomLeftCornerRadius.x
    radii[7] = rect.bottomLeftCornerRadius.y
    addRoundRect(RoundRect(left, top, right, bottom, CornerRadius.Companion.Zero))
}

/**
 * Creates a [MarkerCorneredShape].
 *
 * @param topLeft the size and look of the top-left corner.
 * @param topRight the size and look of the top-right corner.
 * @param bottomRight the size and look of the bottom-right corner.
 * @param bottomLeft the size and look of the bottom-left corner.
 * @param tickSizeDp the tick size.
 */
public fun Shape.markerCorneredShape(
    topLeft: Float,
    topRight: Float,
    bottomRight: Float,
    bottomLeft: Float,
    tickSizeDp: Dp = DEF_MARKER_TICK_SIZE.dp,
): MarkerCorneredShape = MarkerCorneredShape(
    topLeft = topLeft,
    topRight = topRight,
    bottomRight = bottomRight,
    bottomLeft = bottomLeft,
    tickSizeDp = tickSizeDp.value,
)

/**
 * Creates a [MarkerCorneredShape].
 *
 * @param all the size and look of all corners.
 * @param tickSizeDp the tick size.
 */
public fun Shape.markerCorneredShape(
    all: Float,
    tickSizeDp: Dp = DEF_MARKER_TICK_SIZE.dp,
): MarkerCorneredShape = MarkerCorneredShape(
    topLeft = all,
    topRight = all,
    bottomRight = all,
    bottomLeft = all,
    tickSizeDp = tickSizeDp.value,
)

/**
 * Creates a [DashedShape].
 *
 * @param shape the [Shape] from which to create the [DashedShape].
 * @param dashLength the dash length.
 * @param gapLength the gap length.
 * @param fitStrategy the [DashedShape.FitStrategy] to use for the dashes.
 */
//public fun Shapes.dashedShape(
//    shape: Shape,
//    dashLength: Dp,
//    gapLength: Dp,
//    fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize,
//): DashedShape = DashedShape(
//    shape = shape.chartShape(),
//    dashLengthDp = dashLength.value,
//    gapLengthDp = gapLength.value,
//    fitStrategy = fitStrategy,
//)

/**
 * Creates a [DashedShape].
 *
 * @param shape the [ChartShape] from which to create the [DashedShape].
 * @param dashLength the dash length.
 * @param gapLength the gap length.
 * @param fitStrategy the [DashedShape.FitStrategy] to use for the dashes.
 */
public fun Shape.dashedShape(
    shape: Shape,
    dashLength: Dp,
    gapLength: Dp,
    fitStrategy: DashedShape.FitStrategy = DashedShape.FitStrategy.Resize,
): DashedShape = DashedShape(
    shape = shape,
    dashLengthDp = dashLength.value,
    gapLengthDp = gapLength.value,
    fitStrategy = fitStrategy,
)

/**
 * Draws a line.
 * @property color the background color.
 * @property thicknessDp the thickness of the line.
 * @property shape the [Shape] to use for the line.
 * @property dynamicShader an optional [DynamicShader] to apply to the line.
 * @property margins the margins of the line.
 * @property strokeWidthDp the stroke width.
 * @property strokeColor the stroke color.
 */
public open class LineComponent(
    color: Int,
    public var thicknessDp: Float = 2f,
    shape: Shape = RectangleShape,
    dynamicShader: DynamicShader? = null,
    margins: Dimensions = emptyDimensions(),
    strokeWidthDp: Float = 0f,
    strokeColor: Int = Color.Transparent.toArgb(),
) : ShapeComponent(shape, color, dynamicShader, margins, strokeWidthDp, strokeColor) {

    private val MeasureContext.thickness: Float
        get() = thicknessDp.pixels

    /**
     * A convenience function for [draw] that draws the [LineComponent] horizontally.
     */
    public open fun drawHorizontal(
        context: DrawContext,
        left: Float,
        right: Float,
        centerY: Float,
        thicknessScale: Float = 1f,
    ): Unit = with(context) {
        draw(
            context,
            left = left,
            top = centerY - thickness * thicknessScale / 2,
            right = right,
            bottom = centerY + thickness * thicknessScale / 2,
        )
    }

    /**
     * Checks whether the [LineComponent] fits horizontally within the given [boundingBox] with its current
     * [thicknessDp].
     */
    public open fun fitsInHorizontal(
        context: DrawContext,
        left: Float,
        right: Float,
        centerY: Float,
        boundingBox: Rect,
        thicknessScale: Float = 1f,
    ): Boolean = with(context) {
        false
//        boundingBox.contains(
//            Rect(
//                left,
//                centerY - thickness * thicknessScale / 2,
//                right,
//                centerY + thickness * thicknessScale / 2,
//            )
//        )
    }

    /**
     * A convenience function for [draw] that draws the [LineComponent] vertically.
     */
    public open fun drawVertical(
        context: DrawContext,
        top: Float,
        bottom: Float,
        centerX: Float,
        thicknessScale: Float = 1f,
    ): Unit = with(context) {
        draw(
            context,
            left = centerX - thickness * thicknessScale / 2,
            top = top,
            right = centerX + thickness * thicknessScale / 2,
            bottom = bottom,
        )
    }

    /**
     * Checks whether the [LineComponent] fits vertically within the given [boundingBox] with its current [thicknessDp].
     */
    public open fun fitsInVertical(
        context: DrawContext,
        top: Float,
        bottom: Float,
        centerX: Float,
        boundingBox: Rect,
        thicknessScale: Float = 1f,
    ): Boolean = with(context) {
        false
//        boundingBox.contains(
//            centerX - thickness * thicknessScale / 2,
//            top,
//            centerX + thickness * thicknessScale / 2,
//            bottom,
//        )
    }

    /**
     * Checks whether the [LineComponent] vertically intersects the given [boundingBox] with its current [thicknessDp].
     */
    public open fun intersectsVertical(
        context: DrawContext,
        top: Float,
        bottom: Float,
        centerX: Float,
        boundingBox: Rect,
        thicknessScale: Float = 1f,
    ): Boolean = with(context) {
        false
//        boundingBox.intersects(
//            centerX - thickness * thicknessScale / 2,
//            top,
//            centerX + thickness * thicknessScale / 2,
//            bottom,
//        )
    }
}
