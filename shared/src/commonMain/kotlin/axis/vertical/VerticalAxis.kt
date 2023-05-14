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

package com.patrykandpatrick.vico.core.axis.vertical

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.axisLineComponent
import com.patrykandpatrick.vico.compose.axis.axisTickComponent
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DEF_LABEL_COUNT
import com.patrykandpatrick.vico.core.DEF_LABEL_SPACING
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.setTo
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis.HorizontalLabelPosition.Outside
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis.VerticalLabelPosition.Center
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.HorizontalInsets
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.context.Extras
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.context.getOrPutExtra
import com.patrykandpatrick.vico.core.extension.getEnd
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.throwable.UnknownAxisPositionException
import kotlin.math.roundToInt

private const val LABELS_KEY = "labels"
private const val TITLE_ABS_ROTATION_DEGREES = 90f

/**
 * An implementation of [AxisRenderer] used for vertical axes. This class extends [Axis].
 *
 * @see AxisRenderer
 * @see Axis
 */
public class VerticalAxis<Position : AxisPosition.Vertical>(
    override val position: Position,
) : Axis<Position>() {

    private val areLabelsOutsideAtStartOrInsideAtEnd
        get() = horizontalLabelPosition == Outside && position is AxisPosition.Vertical.Start ||
            horizontalLabelPosition == Inside && position is AxisPosition.Vertical.End

    private val textHorizontalPosition: Alignment.Horizontal
        get() = if (areLabelsOutsideAtStartOrInsideAtEnd) Alignment.Start else Alignment.End

    /**
     * The maximum label count.
     */
    public var maxLabelCount: Int = DEF_LABEL_COUNT

    /**
     * The label spacing (in dp).
     */
    public var labelSpacing: Float = DEF_LABEL_SPACING

    /**
     * Defines the horizontal position of each axis label relative to the axis line.
     */
    public var horizontalLabelPosition: HorizontalLabelPosition = Outside

    /**
     * Defines the vertical position of each axis label relative to its corresponding tick.
     */
    public var verticalLabelPosition: VerticalLabelPosition = Center

    override fun drawBehindChart(context: ChartDrawContext): Unit = with(context) {
        val drawLabelCount = getDrawLabelCount(bounds.height.toInt())

        val axisStep = bounds.height / (drawLabelCount - 1)

        var centerY: Float

        for (index in 0 until drawLabelCount) {
            centerY = bounds.bottom - axisStep * index + context.drawScope.guidelineThickness.half

            guideline?.takeIf {
                isNotInRestrictedBounds(
                    left = chartBounds.left,
                    top = centerY - context.drawScope.guidelineThickness.half,
                    right = chartBounds.right,
                    bottom = centerY - context.drawScope.guidelineThickness.half,
                )
            }?.drawHorizontal(
                context = context,
                left = chartBounds.left,
                right = chartBounds.right,
                centerY = centerY,
            )
        }
        axisLine?.drawVertical(
            context = context,
            top = bounds.top,
            bottom = bounds.bottom + drawScope.axisThickness,
            centerX = if (position.isLeft(isLtr = isLtr)) bounds.right else bounds.left,
        )
    }

    override fun drawAboveChart(context: ChartDrawContext): Unit = with(context) {
        val label = label
        val labelCount = getDrawLabelCount(bounds.height.toInt())

        val labels = getLabels(labelCount)

        val tickLeftX = drawScope.getTickLeftX()

        val tickRightX = tickLeftX + drawScope.axisThickness.half + with(drawScope) { tickLengthPx }

        val labelX = if (areLabelsOutsideAtStartOrInsideAtEnd == isLtr) tickLeftX else tickRightX

        var tickCenterY: Float

        (0 until labelCount).forEach { index ->
            tickCenterY = bounds.bottom - bounds.height / (labelCount - 1) * index + drawScope.tickThickness.half

            tick?.drawHorizontal(
                context = context,
                left = tickLeftX,
                right = tickRightX,
                centerY = tickCenterY,
            )

            label ?: return@forEach
            val labelText = labels.getOrNull(index) ?: return@forEach
            drawLabel(
                extras = context,
                drawScope = drawScope,
                label = label,
                labelText = labelText,
                labelX = labelX,
                tickCenterY = tickCenterY,
            )
        }

        title?.let { title ->
            titleComponent?.drawText(
                drawScope = drawScope,
                extras = this,
                text = title,
                textX = if (position.isStart) bounds.getStart(isLtr = isLtr) else bounds.getEnd(isLtr = isLtr),
                textY = bounds.center.y,
                horizontalPosition = if (position.isStart) Alignment.End else Alignment.Start,
                verticalPosition = Alignment.CenterVertically,
                rotationDegrees = TITLE_ABS_ROTATION_DEGREES * if (position.isStart) -1f else 1f,
                maxTextHeight = bounds.height.toInt(),
            )
        }
    }

    private fun ChartDrawContext.drawLabel(
        drawScope: DrawScope,
        extras: Extras,
        label: TextComponent,
        labelText: CharSequence,
        labelX: Float,
        tickCenterY: Float,
    ) {
        val textBounds = label.getTextBounds(extras, drawScope, labelText, rotationDegrees = labelRotationDegrees).let {
            it.translate(
                translateX = labelX,
                translateY = tickCenterY - it.center.y,
            )
        }

        if (
            horizontalLabelPosition == Outside ||
            isNotInRestrictedBounds(
                left = textBounds.left,
                top = textBounds.top,
                right = textBounds.right,
                bottom = textBounds.bottom,
            )
        ) {
            label.drawText(
                drawScope = drawScope,
                extras = this,
                text = labelText,
                textX = labelX,
                textY = tickCenterY,
                horizontalPosition = textHorizontalPosition,
                verticalPosition = verticalLabelPosition.textPosition,
                rotationDegrees = labelRotationDegrees,
                maxTextWidth = when (sizeConstraint) {
                    // Let the `TextComponent` use as much width as it needs, based on the measuring phase.
                    is SizeConstraint.Auto -> Int.MAX_VALUE
                    else -> (bounds.width - with(drawScope) { tickLengthPx } - with(drawScope) { axisThickness.half }).toInt()
                },
            )
        }
    }

    private fun DrawScope.getTickLeftX(): Float {
        val onLeft = position.isLeft(isLtr = layoutDirection == LayoutDirection.Ltr)
        val base = if (onLeft) bounds.right else bounds.left
        return if (onLeft == (horizontalLabelPosition == Outside)) base - axisThickness.half - tickLengthPx else base
    }

    private fun MeasureContext.getDrawLabelCount(availableHeight: Int): Int {
        label?.let { label ->

            val chartValues = chartValuesManager.getChartValues(position)

            fun getLabelHeight(value: Float): Float =
                label.getHeight(
                    extras = this,
                    density = Density(density),
                    // TODO Fix
                    text = value.roundToInt().toString(), //  valueFormatter.formatValue(value, chartValues),
                    rotationDegrees = labelRotationDegrees,
                )

            val avgHeight = arrayOf(
                getLabelHeight(chartValues.minY),
                getLabelHeight((chartValues.maxY + chartValues.minY) / 2),
                getLabelHeight(chartValues.maxY),
            ).maxOrNull().orZero

            return (availableHeight / avgHeight + 1).toInt().coerceAtMost(maxLabelCount)
        }
        return maxLabelCount
    }

    private fun MeasureContext.getLabels(
        maxLabelCount: Int = this@VerticalAxis.maxLabelCount,
    ): List<CharSequence> {
        val chartValues = chartValuesManager.getChartValues(position)
        val cacheKey = LABELS_KEY + position + maxLabelCount
        return getOrPutExtra(key = cacheKey) {
            labels.clear()
            val step = (chartValues.maxY - chartValues.minY) / (maxLabelCount - 1)
            for (index in 0 until maxLabelCount) {
                val value = chartValues.minY + step * index
                labels += valueFormatter.formatValue(value, chartValues)
            }
            labels
        }
    }

    override fun getHorizontalInsets(
        context: MeasureContext,
        availableHeight: Float,
        outInsets: HorizontalInsets,
    ): Unit = with(context) {
        val labels = getLabels(maxLabelCount = getDrawLabelCount(availableHeight.toInt()))

        val desiredWidth = getDesiredWidth(context, labels)

        outInsets.set(
            start = if (position.isStart) desiredWidth else 0f,
            end = if (position.isEnd) desiredWidth else 0f,
        )
    }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        segmentProperties: SegmentProperties,
    ): Unit = with(context) {
        val labelHeight = 0f // label?.getHeight(context = context).orZero
        val lineThickness = with(Density(density)) { maxOf(axisThickness, tickThickness) }
        when (verticalLabelPosition) {
            Center -> outInsets.set(
                top = labelHeight.half - lineThickness,
                bottom = labelHeight.half,
            )
            VerticalLabelPosition.Top -> outInsets.set(
                top = labelHeight - lineThickness,
                bottom = lineThickness,
            )
            VerticalLabelPosition.Bottom -> outInsets.set(
                top = lineThickness.half,
                bottom = labelHeight,
            )
        }
    }

    /**
     * Calculates the optimal width for this [VerticalAxis], accounting for the value of [sizeConstraint].
     */
    private fun getDesiredWidth(
        context: MeasureContext,
        labels: List<CharSequence>,
    ): Float = with(context) {
        when (val constraint = sizeConstraint) {
            is SizeConstraint.Auto -> {
                val titleComponentWidth = title?.let { title ->
                    titleComponent?.getWidth(
                        context = this,
                        text = title,
                        rotationDegrees = TITLE_ABS_ROTATION_DEGREES,
                        height = bounds.height.toInt(),
                    )
                }.orZero
                with(Density(density)) {
                    (getMaxLabelWidth(labels = labels) + titleComponentWidth + axisThickness.half + tickLengthPx)
                        .coerceIn(
                            minimumValue = constraint.minSize.toPx(),
                            maximumValue = constraint.maxSize.toPx()
                        )
                }
            }
            is SizeConstraint.Exact -> with(Density(density)) { constraint.size.toPx() }
            is SizeConstraint.Fraction -> canvasBounds.width * constraint.fraction
            is SizeConstraint.TextWidth -> label?.getWidth(
                context = this,
                text = constraint.text,
                rotationDegrees = labelRotationDegrees,
            ).orZero + with(Density(density)) { tickLengthPx + axisThickness.half }
        }
    }

    private fun MeasureContext.getMaxLabelWidth(labels: List<CharSequence>): Float = when (horizontalLabelPosition) {
        Outside -> label?.let { label ->
            labels.maxOfOrNull { label.getWidth(this, it, rotationDegrees = labelRotationDegrees) }
        }.orZero
        Inside -> 0f
    }

    /**
     * Defines the horizontal position of each of a vertical axis’s labels relative to the axis line.
     */
    public enum class HorizontalLabelPosition {
        Outside, Inside
    }

    /**
     * Defines the vertical position of each of a horizontal axis’s labels relative to the label’s corresponding tick.
     *
     * @param textPosition the label position.
     *
     * @see VerticalPosition
     */
    public enum class VerticalLabelPosition(public val textPosition: Alignment.Vertical) {
        Center(Alignment.CenterVertically),
        Top(Alignment.Top),
        Bottom(Alignment.Bottom),
    }

    /**
     * A subclass of [Axis.Builder] used to build [VerticalAxis] instances.
     */
    public class Builder<Position : AxisPosition.Vertical>(
        builder: Axis.Builder<Position>? = null,
    ) : Axis.Builder<Position>(builder) {
        /**
         * The maximum label count.
         */
        public var maxLabelCount: Int = DEF_LABEL_COUNT

        /**
         * The label spacing (in dp).
         */
        public var labelSpacing: Float = DEF_LABEL_SPACING

        /**
         * Defines the horizontal position of each axis label relative to the axis line.
         */
        public var horizontalLabelPosition: HorizontalLabelPosition = Outside

        /**
         * Defines the vertical position of each axis label relative to its corresponding tick.
         */
        public var verticalLabelPosition: VerticalLabelPosition = Center

        /**
         * Creates a [VerticalAxis] instance with the properties from this [Builder].
         */
        @Suppress("UNCHECKED_CAST")
        public inline fun <reified T : Position> build(): VerticalAxis<T> {
            val position = when (T::class) {
                AxisPosition.Vertical.Start::class -> AxisPosition.Vertical.Start
                AxisPosition.Vertical.End::class -> AxisPosition.Vertical.End
                else -> throw UnknownAxisPositionException()
            } as Position
            return setTo(VerticalAxis(position)).also { axis ->
                axis.maxLabelCount = maxLabelCount
                axis.labelSpacing = labelSpacing
                axis.horizontalLabelPosition = horizontalLabelPosition
                axis.verticalLabelPosition = verticalLabelPosition
            } as VerticalAxis<T>
        }
    }
}

/**
 * A convenience function that creates a [VerticalAxis] instance.
 *
 * @param block a lambda function yielding [VerticalAxis.Builder] as its receiver.
 */
public inline fun <reified Position : AxisPosition.Vertical> createVerticalAxis(
    block: VerticalAxis.Builder<Position>.() -> Unit = {},
): VerticalAxis<Position> = VerticalAxis.Builder<Position>().apply(block).build()

/**
 * Creates a start axis.
 *
 * @param label the [TextComponent] to use for labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for ticks.
 * @param tickLength the length of ticks.
 * @param guideline the [LineComponent] to use for guidelines.
 * @param valueFormatter the [AxisValueFormatter] for the axis.
 * @param sizeConstraint the [Axis.SizeConstraint] for the axis. This determines its width.
 * @param horizontalLabelPosition the horizontal position of the labels along the axis.
 * @param verticalLabelPosition the vertical position of the labels along the axis.
 * @param maxLabelCount the maximum label count.
 * @param labelRotationDegrees the rotation of axis labels in degrees.
 * @param titleComponent an optional [TextComponent] use as the axis title.
 * @param title the axis title.
 */
@Composable
public fun startAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Vertical.Start> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    horizontalLabelPosition: VerticalAxis.HorizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside,
    verticalLabelPosition: VerticalAxis.VerticalLabelPosition = VerticalAxis.VerticalLabelPosition.Center,
    maxLabelCount: Int = DEF_LABEL_COUNT,
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
): VerticalAxis<AxisPosition.Vertical.Start> = createVerticalAxis {
    this.label = label
    this.axis = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    this.tickLength = tickLength
    this.sizeConstraint = sizeConstraint
    this.horizontalLabelPosition = horizontalLabelPosition
    this.verticalLabelPosition = verticalLabelPosition
    this.maxLabelCount = maxLabelCount
    this.labelRotationDegrees = labelRotationDegrees
    this.titleComponent = titleComponent
    this.title = title
}

/**
 * Creates an end axis.
 *
 * @param label the [TextComponent] to use for labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for ticks.
 * @param tickLength the length of ticks.
 * @param guideline the [LineComponent] to use for guidelines.
 * @param valueFormatter the [AxisValueFormatter] for the axis.
 * @param sizeConstraint the [Axis.SizeConstraint] for the axis. This determines its width.
 * @param horizontalLabelPosition the horizontal position of the labels along the axis.
 * @param verticalLabelPosition the vertical position of the labels along the axis.
 * @param maxLabelCount the maximum label count.
 * @param labelRotationDegrees the rotation of axis labels in degrees.
 * @param titleComponent an optional [TextComponent] use as the axis title.
 * @param title the axis title.
 */
@Composable
public fun endAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Vertical.End> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    horizontalLabelPosition: VerticalAxis.HorizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Outside,
    verticalLabelPosition: VerticalAxis.VerticalLabelPosition = VerticalAxis.VerticalLabelPosition.Center,
    maxLabelCount: Int = DEF_LABEL_COUNT,
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
): VerticalAxis<AxisPosition.Vertical.End> = createVerticalAxis {
    this.label = label
    this.axis = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    this.tickLength = tickLength
    this.sizeConstraint = sizeConstraint
    this.horizontalLabelPosition = horizontalLabelPosition
    this.verticalLabelPosition = verticalLabelPosition
    this.maxLabelCount = maxLabelCount
    this.labelRotationDegrees = labelRotationDegrees
    this.titleComponent = titleComponent
    this.title = title
}
