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

package com.patrykandpatrick.vico.core.chart.decoration

import DecimalFormat
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.inBounds
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.extension.ceil
import com.patrykandpatrick.vico.core.extension.floor
import com.patrykandpatrick.vico.core.extension.getEnd
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.median
import extension.isLtr

/**
 * [ThresholdLine] is drawn on top of charts and marks a certain range of y-axis values.
 *
 * @property thresholdRange the range of y-axis values that this [ThresholdLine] will cover.
 * @property thresholdLabel the label of this [ThresholdLine].
 * @property lineComponent the [ShapeComponent] drawn as the threshold line.
 * @property minimumLineThicknessDp the minimal thickness of the threshold line. If the [thresholdRange] implies
 * a smaller thickness, the [minimumLineThicknessDp] will be used as the threshold line’s thickness.
 * @property labelComponent the [TextComponent] used to draw the [thresholdLabel] text.
 * @property labelHorizontalPosition defines the horizontal position of the label.
 * @property labelVerticalPosition defines the vertical position of the label.
 * @property labelRotationDegrees the rotation of the label (in degrees).
 *
 * @see Decoration
 */
public data class ThresholdLine(
    val thresholdRange: ClosedFloatingPointRange<Float>,
//    val thresholdLabel: CharSequence = RANGE_FORMAT.format(
//        decimalFormat.format(thresholdRange.start),
//        decimalFormat.format(thresholdRange.endInclusive),
//    ),
    val lineComponent: ShapeComponent = ShapeComponent(),
    val minimumLineThicknessDp: Float = DefaultDimens.THRESHOLD_LINE_THICKNESS,
    val labelComponent: TextComponent = textComponent(),
    val labelHorizontalPosition: LabelHorizontalPosition = LabelHorizontalPosition.Start,
    val labelVerticalPosition: LabelVerticalPosition = LabelVerticalPosition.Top,
    val labelRotationDegrees: Float = 0f,
) : Decoration {

    /**
     * An alternative constructor that accepts a single y-axis value as opposed to a range.
     *
     * @property thresholdValue the value on the y-axis that this [ThresholdLine] will cover.
     * @property thresholdLabel the label of this [ThresholdLine].
     * @property lineComponent the [ShapeComponent] drawn as the threshold line.
     * @property minimumLineThicknessDp the minimal thickness of the threshold line. If the [thresholdRange] implies
     * a smaller thickness, the [minimumLineThicknessDp] will be used as the threshold line’s thickness.
     * @property labelComponent the [TextComponent] used to draw the [thresholdLabel] text.
     * @property labelHorizontalPosition defines the horizontal position of the label.
     * @property labelVerticalPosition defines the vertical position of the label.
     * @property labelRotationDegrees the rotation of the label (in degrees).
     */
    public constructor(
        thresholdValue: Float,
//        thresholdLabel: CharSequence = decimalFormat.format(thresholdValue),
        lineComponent: ShapeComponent = ShapeComponent(),
        minimumLineThicknessDp: Float = DefaultDimens.THRESHOLD_LINE_THICKNESS,
        labelComponent: TextComponent = textComponent(),
        labelHorizontalPosition: LabelHorizontalPosition = LabelHorizontalPosition.Start,
        labelVerticalPosition: LabelVerticalPosition = LabelVerticalPosition.Top,
        labelRotationDegrees: Float = 0f,
    ) : this(
        thresholdRange = thresholdValue..thresholdValue,
//        thresholdLabel = thresholdLabel,
        lineComponent = lineComponent,
        minimumLineThicknessDp = minimumLineThicknessDp,
        labelComponent = labelComponent,
        labelHorizontalPosition = labelHorizontalPosition,
        labelVerticalPosition = labelVerticalPosition,
        labelRotationDegrees = labelRotationDegrees,
    )

    override fun onDrawAboveChart(
        drawScope: DrawScope,
        context: ChartDrawContext,
        bounds: Rect,
    ): Unit = with(context) {
        with(context.drawScope) {
            val chartValues = chartValuesManager.getChartValues()

            val valueRange = chartValues.maxY - chartValues.minY

            val centerY =
                bounds.bottom - (thresholdRange.median - chartValues.minY) / valueRange * bounds.height

            val topY = minOf(
                bounds.bottom - (thresholdRange.endInclusive - chartValues.minY) / valueRange * bounds.height,
                centerY - minimumLineThicknessDp.dp.toPx().half,
            ).ceil
            val bottomY = maxOf(
                bounds.bottom - (thresholdRange.start - chartValues.minY) / valueRange * bounds.height,
                centerY + minimumLineThicknessDp.dp.toPx().half,
            ).floor
            val textY = when (labelVerticalPosition) {
                LabelVerticalPosition.Top -> topY
                LabelVerticalPosition.Bottom -> bottomY
            }

            lineComponent.draw(
                drawScope = context.drawScope,
                left = bounds.left,
                right = bounds.right,
                top = topY,
                bottom = bottomY,
            )
            labelComponent.drawText(
                drawScope = drawScope,
                extras = context,
                text = "", // thresholdLabel,
                maxTextWidth = bounds.width.toInt(),
                textX = when (labelHorizontalPosition) {
                    LabelHorizontalPosition.Start -> bounds.getStart(isLtr = isLtr)
                    LabelHorizontalPosition.End -> bounds.getEnd(isLtr = isLtr)
                },
                textY = textY,
                horizontalPosition = labelHorizontalPosition.position,
                verticalPosition = labelVerticalPosition.position.inBounds(
                    bounds = bounds,
                    componentHeight = 0f, //labelComponent.getHeight(
//                    context = context,
//                    text = "", // thresholdLabel,
//                    rotationDegrees = labelRotationDegrees,
//                ),
                    y = textY,
                ),
                rotationDegrees = labelRotationDegrees,
            )
        }
    }

    /**
     * Defines the horizontal position of a [ThresholdLine]’s label.
     *
     * @property position the label position.
     */
    public enum class LabelHorizontalPosition(public val position: Alignment.Horizontal) {
        Start(Alignment.End),
        End(Alignment.Start),
    }

    /**
     * Defines the vertical position of a [ThresholdLine]’s label.
     *
     * @property position the label position.
     */
    public enum class LabelVerticalPosition(public val position: Alignment.Vertical) {
        Top(Alignment.Top),
        Bottom(Alignment.Bottom),
    }

    private companion object {
        const val RANGE_FORMAT = "%s–%s"
        val decimalFormat = DecimalFormat("#.##")
    }
}
