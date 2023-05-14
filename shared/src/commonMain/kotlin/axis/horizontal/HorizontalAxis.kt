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

package com.patrykandpatrick.vico.core.axis.horizontal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.axisLineComponent
import com.patrykandpatrick.vico.compose.axis.axisTickComponent
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.setTo
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.doubled
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.throwable.UnknownAxisPositionException
import kotlin.math.abs
import kotlin.math.ceil

/**
 * An implementation of [AxisRenderer] used for horizontal axes. This class extends [Axis].
 *
 * @see AxisRenderer
 * @see Axis
 */
public class HorizontalAxis<Position : AxisPosition.Horizontal>(
    override val position: Position,
) : Axis<Position>() {

    private val AxisPosition.Horizontal.textVerticalPosition: Alignment.Vertical
        get() = if (isBottom) Alignment.Bottom else Alignment.Top

    /**
     * Defines the tick placement.
     */
    public var tickPosition: TickPosition = TickPosition.Edge

    override fun drawBehindChart(context: ChartDrawContext): Unit = with(context) {
        canvas.save()
        val tickMarkTop: Float = if (position.isBottom) bounds.top else bounds.bottom - with(context.drawScope) { tickLengthPx }
        val tickMarkBottom = tickMarkTop + with(drawScope) { axisThickness } + with(context.drawScope) { tickLengthPx }
        val chartValues = chartValuesManager.getChartValues()
        val step = chartValues.xStep

        canvas.clipRect(
            bounds.left - tickPosition.getTickInset(with(drawScope) { tickThickness }),
            minOf(bounds.top, chartBounds.top),
            bounds.right + tickPosition.getTickInset(with(drawScope) { tickThickness }),
            maxOf(bounds.bottom, chartBounds.bottom),
        )

        val tickDrawStep = segmentProperties.segmentWidth
        val scrollAdjustment = (abs(x = horizontalScroll) / tickDrawStep).toInt()
        val textY = if (position.isBottom) tickMarkBottom else tickMarkTop

        val labelPositionOffset = when (segmentProperties.labelPositionOrDefault) {
            LabelPosition.Start -> 0f
            LabelPosition.Center -> tickDrawStep.half
        }

        var textCenter: Float = bounds.getStart(isLtr = isLtr) + layoutDirectionMultiplier *
            (labelPositionOffset + tickDrawStep * scrollAdjustment) - horizontalScroll

        var tickCenter = getTickDrawCenter(tickPosition, horizontalScroll, tickDrawStep, scrollAdjustment, textCenter)

        forEachEntity(
            scrollAdjustment = scrollAdjustment,
            step = step,
            xRange = chartValues.minX..chartValues.maxX,
        ) { x, shouldDrawLines, shouldDrawLabel ->

            guideline
                ?.takeIf {
                    shouldDrawLines &&
                        it.fitsInVertical(
                            context = context,
                            top = chartBounds.top,
                            bottom = chartBounds.bottom,
                            centerX = tickCenter,
                            boundingBox = chartBounds,
                        )
                }?.drawVertical(
                    context = context,
                    top = chartBounds.top,
                    bottom = chartBounds.bottom,
                    centerX = tickCenter,
                )

            tick
                .takeIf { shouldDrawLines }
                ?.drawVertical(context = context, top = tickMarkTop, bottom = tickMarkBottom, centerX = tickCenter)

            label
                .takeIf { shouldDrawLabel }
                ?.drawText(
                    drawScope = drawScope,
                    extras = context,
                    text = valueFormatter.formatValue(x, chartValues),
                    textX = textCenter,
                    textY = textY,
                    verticalPosition = position.textVerticalPosition,
                    maxTextWidth = getMaxTextWidth(
                        tickDrawStep = tickDrawStep,
                        spacing = tickPosition.spacing,
                        textX = textCenter,
                        bounds = chartBounds,
                    ),
                    maxTextHeight = (bounds.height - with(drawScope) { tickLengthPx } - with(drawScope) { axisThickness }.half).toInt(),
                    rotationDegrees = labelRotationDegrees,
                )

            tickCenter += layoutDirectionMultiplier * tickDrawStep
            textCenter += layoutDirectionMultiplier * tickDrawStep
        }

        axisLine?.drawHorizontal(
            context = context,
            left = chartBounds.left,
            right = chartBounds.right,
            centerY = (if (position.isBottom) bounds.top else bounds.bottom) + with(drawScope) { axisThickness }.half,
        )

        title?.let { title ->
            titleComponent?.drawText(
                drawScope = drawScope,
                extras = context,
                textX = bounds.center.x,
                textY = if (position.isTop) bounds.top else bounds.bottom,
                verticalPosition = if (position.isTop) Alignment.Bottom else Alignment.Top,
                maxTextWidth = bounds.width.toInt(),
                text = title,
            )
        }

        canvas.restore()
    }

    override fun drawAboveChart(context: ChartDrawContext): Unit = Unit

    private fun getEntryLength(segmentWidth: Float) =
        ceil(bounds.width / segmentWidth).toInt() + 1

    private inline fun ChartDrawContext.forEachEntity(
        scrollAdjustment: Int,
        step: Float,
        xRange: ClosedFloatingPointRange<Float>,
        action: (x: Float, shouldDrawLines: Boolean, shouldDrawLabel: Boolean) -> Unit,
    ) {
        val entryLength = getEntryLength(segmentProperties.segmentWidth)

        for (index in 0 until tickPosition.getTickCount(entryLength = entryLength)) {
            val relativeX = (scrollAdjustment + index) * step
            val x = relativeX + xRange.start

            val firstEntityConditionsMet = relativeX != 0f ||
                !segmentProperties.labelPositionOrDefault.skipFirstEntity ||
                tickPosition.offset > 0

            val shouldDrawLines = relativeX / step >= tickPosition.offset &&
                (relativeX / step - tickPosition.offset) % tickPosition.spacing == 0f &&
                firstEntityConditionsMet

            action(
                x,
                shouldDrawLines,
                shouldDrawLines && x in xRange && index < entryLength,
            )
        }
    }

    private fun DrawContext.getTickDrawCenter(
        tickPosition: TickPosition,
        scrollX: Float,
        tickDrawStep: Float,
        scrollAdjustment: Int,
        textDrawCenter: Float,
    ) = when (tickPosition) {
        is TickPosition.Center -> textDrawCenter
        is TickPosition.Edge -> bounds.getStart(isLtr = isLtr) + tickDrawStep * tickPosition.offset +
            layoutDirectionMultiplier * tickDrawStep * scrollAdjustment - scrollX
    }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        segmentProperties: SegmentProperties,
    ): Unit = with(context) {
        with(outInsets) {
            setHorizontal(tickPosition.getTickInset(with(Density(density)) { tickThickness }))
            top = if (position.isTop) getDesiredHeight(context, segmentProperties) else 0f
            bottom = if (position.isBottom) getDesiredHeight(context, segmentProperties) else 0f
        }
    }

    private fun getDesiredHeight(
        context: MeasureContext,
        segmentProperties: SegmentProperties,
    ): Float = with(context) {
        val labelWidth =
            if (isHorizontalScrollEnabled) {
                segmentProperties.scaled(scale = chartScale).segmentWidth.toInt() * tickPosition.spacing
            } else {
                Int.MAX_VALUE
            }

        when (val constraint = sizeConstraint) {
            is SizeConstraint.Auto -> {
                val labelHeight = label?.let { label ->
                    getLabelsToMeasure().maxOf { labelText ->
                        label.getHeight(
                            extras = this,
                            density = Density(density),
                            text = labelText,
                            width = labelWidth,
                            rotationDegrees = labelRotationDegrees,
                        ).orZero
                    }
                }.orZero
                val titleComponentHeight = title?.let { title ->
                    titleComponent?.getHeight(
                        extras = this,
                        density = Density(density),
                        width = bounds.width.toInt(),
                        text = title,
                    )
                }.orZero
                with(Density(density)) {
                    (labelHeight + titleComponentHeight + (if (position.isBottom) Density(density).axisThickness else 0f) +
                            with(Density(density)) { tickLengthPx })
                        .coerceAtMost(maximumValue = canvasBounds.height / MAX_HEIGHT_DIVISOR)
                        .coerceIn(
                            minimumValue = constraint.minSize.toPx(),
                            maximumValue = constraint.maxSize.toPx()
                        )
                }
            }
            is SizeConstraint.Exact -> with(Density(density)) { constraint.size.toPx() }
            is SizeConstraint.Fraction -> canvasBounds.height * constraint.fraction
            is SizeConstraint.TextWidth ->  label?.getHeight(
                extras = this,
                density = Density(density),
                text = constraint.text,
                width = labelWidth,
                rotationDegrees = labelRotationDegrees,
            ).orZero
        }
    }

    private fun MeasureContext.getLabelsToMeasure(): List<CharSequence> {
        val chartValues = chartValuesManager.getChartValues()

        return listOf(
            chartValues.minX,
            (chartValues.maxX - chartValues.minX).half,
            chartValues.maxX,
        ).map { x -> valueFormatter.formatValue(value = x, chartValues = chartValues) }
    }

    /**
     * Defines the position of a horizontal axis’s labels.
     */
    public enum class LabelPosition(internal val skipFirstEntity: Boolean) {
        Start(skipFirstEntity = true),
        Center(skipFirstEntity = false),
    }

    /**
     * Defines the position of a horizontal axis’s ticks. [HorizontalAxis.TickPosition.Center] allows for offset and
     * spacing customization.
     *
     * @param offset the index at which ticks and labels start to be drawn. The default is 0.
     * @param spacing defines how often ticks should be drawn, where 1 means a tick is drawn for each entry,
     * 2 means a tick is drawn for every second entry, and so on.
     */
    public sealed class TickPosition(
        public val offset: Int,
        public val spacing: Int,
    ) {

        /**
         * Returns the tick count required by this [TickPosition].
         */
        public abstract fun getTickCount(entryLength: Int): Int

        /**
         * Returns the chart inset required by this [TickPosition].
         */
        public abstract fun getTickInset(tickThickness: Float): Float

        /**
         * A tick will be drawn at either edge of each chart segment.
         *
         * ```
         * —————————————————
         * |   |   |   |   |
         *   1   2   3   4
         * ```
         */
        public object Edge : TickPosition(offset = 0, spacing = 1) {

            override fun getTickCount(entryLength: Int): Int = entryLength + 1

            override fun getTickInset(tickThickness: Float): Float = tickThickness.half
        }

        /**
         * A tick will be drawn at the center of each chart segment.
         *
         * ```
         * ————————————————
         *   |   |   |   |
         *   1   2   3   4
         * ```
         *
         * [offset] is the index at which ticks and labels start to be drawn. Setting [offset] to 2 gives this result:
         *
         * ```
         * ————————————————
         *           |   |
         *           3   4
         * ```
         *
         * [spacing] defines how often ticks should be drawn. Setting [spacing] to 2 gives this result:
         *
         * ```
         * ————————————————
         *   |       |
         *   1       3
         * ```
         */
        public class Center(
            offset: Int = 0,
            spacing: Int = 1,
        ) : TickPosition(offset = offset, spacing = spacing) {

            public constructor(spacing: Int) : this(offset = spacing, spacing = spacing)

            init {
                require(offset >= 0) { "`offset` cannot be negative. Received $offset." }
                require(spacing >= 1) { "`spacing` cannot be less than 1. Received $spacing." }
            }

            override fun getTickCount(entryLength: Int): Int = entryLength

            override fun getTickInset(tickThickness: Float): Float = 0f
        }
    }

    /**
     * A subclass of [Axis.Builder] used to build [HorizontalAxis] instances.
     */
    public class Builder<Position : AxisPosition.Horizontal>(
        builder: Axis.Builder<Position>? = null,
    ) : Axis.Builder<Position>(builder) {

        /**
         * Defines the tick placement.
         */
        public var tickPosition: TickPosition = TickPosition.Edge

        /**
         * Creates a [HorizontalAxis] instance with the properties from this [Builder].
         */
        @Suppress("UNCHECKED_CAST")
        public inline fun <reified T : Position> build(): HorizontalAxis<T> {
            val position = when (T::class) {
                AxisPosition.Horizontal.Top::class -> AxisPosition.Horizontal.Top
                AxisPosition.Horizontal.Bottom::class -> AxisPosition.Horizontal.Bottom
                else -> throw UnknownAxisPositionException()
            } as Position
            return setTo(HorizontalAxis(position = position)).also { axis ->
                axis.tickPosition = tickPosition
            } as HorizontalAxis<T>
        }
    }

    internal companion object {
        const val MAX_HEIGHT_DIVISOR = 3f

        private fun MeasureContext.getMaxTextWidth(
            tickDrawStep: Float,
            spacing: Int,
            textX: Float,
            bounds: Rect,
        ): Int {
            val baseWidth = tickDrawStep * spacing
            val left = textX - baseWidth.half
            val right = textX + baseWidth.half

            return when {
                isHorizontalScrollEnabled -> baseWidth
                bounds.left > left -> baseWidth - (bounds.left - left).doubled
                bounds.right < right -> baseWidth - (right - bounds.right).doubled
                else -> baseWidth
            }.toInt()
        }
    }
}

/**
 * A convenience function that creates a [HorizontalAxis] instance.
 *
 * @param block a lambda function yielding [HorizontalAxis.Builder] as its receiver.
 */
public inline fun <reified Position : AxisPosition.Horizontal> createHorizontalAxis(
    block: HorizontalAxis.Builder<Position>.() -> Unit = {},
): HorizontalAxis<Position> = HorizontalAxis.Builder<Position>().apply(block).build()

/**
 * Creates a top axis.
 *
 * @param label the [TextComponent] to use for labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for ticks.
 * @param tickLength the length of ticks.
 * @param tickPosition defines the position of ticks. [HorizontalAxis.TickPosition.Center] allows for using a custom
 * offset and spacing for both ticks and labels.
 * @param guideline the [LineComponent] to use for guidelines.
 * @param valueFormatter the [AxisValueFormatter] for the axis.
 * @param sizeConstraint the [Axis.SizeConstraint] for the axis. This determines its height.
 * @param labelRotationDegrees the rotation of axis labels in degrees.
 * @param titleComponent an optional [TextComponent] use as the axis title.
 * @param title the axis title.
 */
@Composable
public fun topAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    tickPosition: HorizontalAxis.TickPosition = HorizontalAxis.TickPosition.Edge,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Top> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
): HorizontalAxis<AxisPosition.Horizontal.Top> = createHorizontalAxis {
    this.label = label
    this.axis = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    this.tickLength = tickLength
    this.tickPosition = tickPosition
    this.sizeConstraint = sizeConstraint
    this.labelRotationDegrees = labelRotationDegrees
    this.titleComponent = titleComponent
    this.title = title
}

/**
 * Creates a bottom axis.
 *
 * @param label the [TextComponent] to use for labels.
 * @param axis the [LineComponent] to use for the axis line.
 * @param tick the [LineComponent] to use for ticks.
 * @param tickLength the length of ticks.
 * @param tickPosition defines the position of ticks. [HorizontalAxis.TickPosition.Center] allows for using a custom
 * offset and spacing for both ticks and labels.
 * @param guideline the [LineComponent] to use for guidelines.
 * @param valueFormatter the [AxisValueFormatter] for the axis.
 * @param sizeConstraint the [Axis.SizeConstraint] for the axis. This determines its height.
 * @param labelRotationDegrees the rotation of axis labels in degrees.
 * @param titleComponent an optional [TextComponent] use as the axis title.
 * @param title the axis title.
 */
@Composable
public fun bottomAxis(
    label: TextComponent? = axisLabelComponent(),
    axis: LineComponent? = axisLineComponent(),
    tick: LineComponent? = axisTickComponent(),
    tickLength: Dp = currentChartStyle.axis.axisTickLength,
    tickPosition: HorizontalAxis.TickPosition = HorizontalAxis.TickPosition.Edge,
    guideline: LineComponent? = axisGuidelineComponent(),
    valueFormatter: AxisValueFormatter<AxisPosition.Horizontal.Bottom> = DecimalFormatAxisValueFormatter(),
    sizeConstraint: Axis.SizeConstraint = Axis.SizeConstraint.Auto(),
    titleComponent: TextComponent? = null,
    title: CharSequence? = null,
    labelRotationDegrees: Float = currentChartStyle.axis.axisLabelRotationDegrees,
): HorizontalAxis<AxisPosition.Horizontal.Bottom> = createHorizontalAxis {
    this.label = label
    this.axis = axis
    this.tick = tick
    this.guideline = guideline
    this.valueFormatter = valueFormatter
    this.tickLength = tickLength
    this.tickPosition = tickPosition
    this.sizeConstraint = sizeConstraint
    this.labelRotationDegrees = labelRotationDegrees
    this.titleComponent = titleComponent
    this.title = title
}
