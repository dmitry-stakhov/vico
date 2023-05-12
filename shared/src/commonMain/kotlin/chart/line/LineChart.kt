/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.line

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DefaultAlpha
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.chart.BaseChart
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.decoration.Decoration
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.segmentWidth
import com.patrykandpatrick.vico.core.chart.forEachInIndexed
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec
import com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec.PointConnector
import com.patrykandpatrick.vico.core.chart.put
import com.patrykandpatrick.vico.core.chart.segment.MutableSegmentProperties
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.VerticalPosition
import com.patrykandpatrick.vico.core.component.text.inBounds
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.getRepeating
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.rangeWith
import com.patrykandpatrick.vico.core.formatter.DecimalFormatValueFormatter
import com.patrykandpatrick.vico.core.formatter.ValueFormatter
import com.patrykandpatrick.vico.core.marker.Marker
import kotlin.math.max
import kotlin.math.min

/**
 * Creates a [LineChart].
 *
 * @param lines the [LineChart.LineSpec]s to use for the lines. This list is iterated through as many times as there
 * are lines.
 * @param decorations the list of [Decoration]s that will be added to the [LineChart].
 * @param persistentMarkers maps x-axis values to persistent [Marker]s.
 * @param pointPosition the horizontal position of each point in its corresponding segment.
 * @param axisValuesOverrider overrides the minimum and maximum x-axis and y-axis values.
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 *
 * @see Chart
 * @see ColumnChart
 */
@Composable
public fun lineChart(
    lines: List<LineSpec> = currentChartStyle.lineChart.lines,
    spacing: Dp = currentChartStyle.lineChart.spacing,
    pointPosition: LineChart.PointPosition = LineChart.PointPosition.Center,
    decorations: List<Decoration>? = null,
    persistentMarkers: Map<Float, Marker>? = null,
    axisValuesOverrider: AxisValuesOverrider<ChartEntryModel>? = null,
    targetVerticalAxisPosition: AxisPosition.Vertical? = null,
): LineChart = remember { LineChart() }.apply {
    this.lines = lines
    this.spacingDp = spacing.value
    this.pointPosition = pointPosition
    this.axisValuesOverrider = axisValuesOverrider
    this.targetVerticalAxisPosition = targetVerticalAxisPosition
    decorations?.also(::setDecorations)
    persistentMarkers?.also(::setPersistentMarkers)
}

/**
 * Creates a [LineChart.LineSpec] for use in [LineChart]s.
 *
 * @param lineColor the color of the line.
 * @param lineThickness the thickness of the line.
 * @param lineBackgroundShader an optional [DynamicShader] to use for the area below the line.
 * @param lineCap the stroke cap for the line.
 * @param point an optional [Component] that can be drawn at a given point on the line.
 * @param pointSize the size of the [point].
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the line.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels in degrees.
 * @param pointConnector the [LineSpec.PointConnector] for the line.
 *
 * @see LineChart
 * @see LineChart.LineSpec
 */
public fun lineSpec(
    lineColor: Color,
    lineThickness: Dp = DefaultDimens.LINE_THICKNESS.dp,
    lineBackgroundShader: DynamicShader? = DynamicShaders.fromBrush(
        brush = Brush.verticalGradient(
            listOf(
                lineColor.copy(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                lineColor.copy(alpha = DefaultAlpha.LINE_BACKGROUND_SHADER_END),
            ),
        ),
    ),
    lineCap: StrokeCap = StrokeCap.Round,
    point: Component? = null,
    pointSize: Dp = DefaultDimens.POINT_SIZE.dp,
    dataLabel: TextComponent? = null,
    dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
    dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
    dataLabelRotationDegrees: Float = 0f,
    pointConnector: LineSpec.PointConnector = DefaultPointConnector(),
): LineSpec = LineSpec(
    lineColor = lineColor.toArgb(),
    lineThicknessDp = lineThickness.value,
    lineBackgroundShader = lineBackgroundShader,
    lineCap = lineCap,
    point = point,
    pointSizeDp = pointSize.value,
    dataLabel = dataLabel,
    dataLabelVerticalPosition = dataLabelVerticalPosition,
    dataLabelValueFormatter = dataLabelValueFormatter,
    dataLabelRotationDegrees = dataLabelRotationDegrees,
    pointConnector = pointConnector,
)

/**
 * [LineChart] displays data as a continuous line.
 *
 * @param lines a [List] of [LineSpec]s defining the style of each line.
 * @param spacingDp the spacing between each [LineSpec.point] (in dp).
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 * @param pointPosition the horizontal position of each point in its corresponding segment.
 */
public open class LineChart(
    public var lines: List<LineSpec> = listOf(LineSpec()),
    public var spacingDp: Float = DefaultDimens.POINT_SPACING,
    public var targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    public var pointPosition: PointPosition = PointPosition.Center,
) : BaseChart<ChartEntryModel>() {

    /**
     * Creates a [LineChart] with a common style for all lines.
     *
     * @param line a [LineSpec] defining the style of each line.
     * @param spacingDp the spacing between each [LineSpec.point] (in dp).
     * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
     * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
     * @param pointPosition the horizontal position of each point in its corresponding segment.
     */
    public constructor(
        line: LineSpec,
        spacingDp: Float,
        targetVerticalAxisPosition: AxisPosition.Vertical? = null,
        pointPosition: PointPosition = PointPosition.Center,
    ) : this(listOf(line), spacingDp, targetVerticalAxisPosition, pointPosition)

    /**
     * Defines the appearance of a line in a line chart.
     *
     * @param lineColor the color of the line.
     * @param lineThicknessDp the thickness of the line (in dp).
     * @param lineBackgroundShader an optional [DynamicShader] to use for the area below the line.
     * @param lineCap the stroke cap for the line.
     * @param point an optional [Component] that can be drawn at a given point on the line.
     * @param pointSizeDp the size of the [point] (in dp).
     * @param dataLabel an optional [TextComponent] to use for data labels.
     * @param dataLabelVerticalPosition the vertical position of data labels relative to the line.
     * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
     * @param dataLabelRotationDegrees the rotation of data labels (in degrees).
     * @param pointConnector the [PointConnector] for the line.
     */
    public open class LineSpec(
        lineColor: Int = Color.LightGray.toArgb(),
        public var lineThicknessDp: Float = DefaultDimens.LINE_THICKNESS,
        public var lineBackgroundShader: DynamicShader? = null,
        public var lineCap: StrokeCap = StrokeCap.Round,
        public var point: Component? = null,
        public var pointSizeDp: Float = DefaultDimens.POINT_SIZE,
        public var dataLabel: TextComponent? = null,
        public var dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
        public var dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
        public var dataLabelRotationDegrees: Float = 0f,
        public var pointConnector: PointConnector = DefaultPointConnector(),
    ) {

        /**
         * Defines the appearance of a line in a line chart.
         *
         * @param lineColor the color of the line.
         * @param lineThicknessDp the thickness of the line (in dp).
         * @param lineBackgroundShader an optional [DynamicShader] to use for the area below the line.
         * @param lineCap the stroke cap for the line.
         * @param cubicStrength the strength of the cubic bezier curve between each point on the line.
         * @param point an optional [Component] that can be drawn at a given point on the line.
         * @param pointSizeDp the size of the [point] (in dp).
         * @param dataLabel an optional [TextComponent] to use for data labels.
         * @param dataLabelVerticalPosition the vertical position of data labels relative to the line.
         * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
         * @param dataLabelRotationDegrees the rotation of data labels in degrees.
         */
        @Deprecated(
            message = """Rather than using this constructor and its `cubicStrength` parameter, use the primary
                constructor and provide a `DefaultPointConnector` instance with a custom `cubicStrength` via the
                `pointConnector` parameter.""",
            replaceWith = ReplaceWith(
                expression = """LineSpec(
                        lineColor = lineColor,
                        lineThicknessDp = lineThicknessDp,
                        lineBackgroundShader = lineBackgroundShader,
                        lineCap = lineCap,
                        point = point,
                        pointSizeDp = pointSizeDp,
                        dataLabel = dataLabel,
                        dataLabelVerticalPosition = dataLabelVerticalPosition,
                        dataLabelValueFormatter = dataLabelValueFormatter,
                        dataLabelRotationDegrees = dataLabelRotationDegrees,
                        pointPosition = pointPosition,
                        pointConnector = DefaultPointConnector(cubicStrength = cubicStrength),
                    )""",
                imports = arrayOf("com.patrykandpatrick.vico.core.chart.DefaultPointConnector"),
            ),
            level = DeprecationLevel.ERROR,
        )
        public constructor(
            lineColor: Int = Color.LightGray.toArgb(),
            lineThicknessDp: Float = DefaultDimens.LINE_THICKNESS,
            lineBackgroundShader: DynamicShader? = null,
            lineCap: StrokeCap = StrokeCap.Round,
            cubicStrength: Float,
            point: Component? = null,
            pointSizeDp: Float = DefaultDimens.POINT_SIZE,
            dataLabel: TextComponent? = null,
            dataLabelVerticalPosition: VerticalPosition = VerticalPosition.Top,
            dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
            dataLabelRotationDegrees: Float = 0f,
        ) : this(
            lineColor = lineColor,
            lineThicknessDp = lineThicknessDp,
            lineBackgroundShader = lineBackgroundShader,
            lineCap = lineCap,
            point = point,
            pointSizeDp = pointSizeDp,
            dataLabel = dataLabel,
            dataLabelVerticalPosition = dataLabelVerticalPosition,
            dataLabelValueFormatter = dataLabelValueFormatter,
            dataLabelRotationDegrees = dataLabelRotationDegrees,
            pointConnector = DefaultPointConnector(cubicStrength = cubicStrength),
        )

        /**
         * Returns `true` if the [lineBackgroundShader] is not null, and `false` otherwise.
         */
        public val hasLineBackgroundShader: Boolean
            get() = lineBackgroundShader != null

        protected val linePaint: Paint = Paint().apply {
            isAntiAlias = true
            style = PaintingStyle.Stroke
            color = Color(lineColor)
            strokeCap = lineCap
        }

        protected val lineBackgroundPaint: Paint = Paint().apply {
            isAntiAlias = true
        }

        /**
         * The color of the line.
         */
        public var lineColor: Int
            get() = linePaint.color.toArgb()
            set(value) {
                linePaint.color = Color(value)
            }

        /**
         * The stroke cap for the line.
         */
        public var lineStrokeCap: StrokeCap by linePaint::strokeCap

        /**
         * Draws a [point] at the given [x] and [y] coordinates, using the provided [context].
         *
         * @see Component
         */
        public fun drawPoint(
            context: DrawContext,
            x: Float,
            y: Float,
        ): Unit = with(context) {
            point?.drawPoint(context, x, y, pointSizeDp.pixels.half)
        }

        /**
         * Draws the line.
         */
        public fun drawLine(context: DrawContext, path: Path): Unit = with(context) {
            linePaint.strokeWidth = lineThicknessDp.pixels
            canvas.drawPath(path, linePaint)
        }

        /**
         * Draws the line background.
         */
        public fun drawBackgroundLine(context: DrawContext, bounds: Rect, path: Path): Unit = with(context) {
            lineBackgroundPaint.shader = lineBackgroundShader
                ?.provideShader(
                    context = context,
                    left = bounds.left,
                    top = bounds.top,
                    right = bounds.right,
                    bottom = bounds.bottom,
                )

            canvas.drawPath(path, lineBackgroundPaint)
        }

        /**
         * Defines the shape of a line in a line chart by specifying how points are to be connected.
         *
         * @see DefaultPointConnector
         */
        public interface PointConnector {

            /**
             * Draws a line between two points.
             */
            @LongParameterListDrawFunction
            public fun connect(
                path: Path,
                prevX: Float,
                prevY: Float,
                x: Float,
                y: Float,
                segmentProperties: SegmentProperties,
                bounds: Rect,
            )
        }
    }

    /**
     * The [Path] used to draw the lines, each of which corresponds to a [LineSpec].
     */
    protected val linePath: Path = Path()

    /**
     * The [Path] used to draw the backgrounds of the lines, each of which corresponds to a [LineSpec].
     */
    protected val lineBackgroundPath: Path = Path()

    /**
     * The chart’s [MutableSegmentProperties] instance, which holds information about the segment properties.
     */
    protected val segmentProperties: MutableSegmentProperties = MutableSegmentProperties()

    override val entryLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()

    override fun drawChart(
        context: ChartDrawContext,
        model: ChartEntryModel,
    ): Unit = with(context) {
        resetTempData()

        val (cellWidth, spacing) = segmentProperties

        model.entries.forEachIndexed { entryListIndex, entries ->

            linePath.reset()
//            linePath.rewind()
            lineBackgroundPath.reset()
//            lineBackgroundPath.rewind()
            val component = lines.getRepeating(entryListIndex)

            var prevX = bounds.getStart(isLtr = isLtr)
            var prevY = bounds.bottom

            val drawingStartAlignmentCorrection = layoutDirectionMultiplier *
                when (pointPosition) {
                    PointPosition.Start -> 0f
                    PointPosition.Center -> (spacing + cellWidth).half
                }

            val drawingStart = bounds.getStart(isLtr = isLtr) + drawingStartAlignmentCorrection - horizontalScroll

            forEachPointWithinBoundsIndexed(
                entries = entries,
                segment = segmentProperties,
                drawingStart = drawingStart,
            ) { entryIndex, entry, x, y ->
                if (linePath.isEmpty) {
                    linePath.moveTo(x, y)
                    if (component.hasLineBackgroundShader) {
                        lineBackgroundPath.moveTo(x, bounds.bottom)
                        lineBackgroundPath.lineTo(x, y)
                    }
                } else {
                    component.pointConnector.connect(
                        path = linePath,
                        prevX = prevX,
                        prevY = prevY,
                        x = x,
                        y = y,
                        segmentProperties = segmentProperties,
                        bounds = bounds,
                    )
                    if (component.hasLineBackgroundShader) {
                        component.pointConnector.connect(
                            path = lineBackgroundPath,
                            prevX = prevX,
                            prevY = prevY,
                            x = x,
                            y = y,
                            segmentProperties = segmentProperties,
                            bounds = bounds,
                        )
                    }
                }
                prevX = x
                prevY = y

                if (x in bounds.left..bounds.right) {
                    entryLocationMap.put(
                        x = x,
                        y = y.coerceIn(bounds.top, bounds.bottom),
                        entry = entry,
                        color = component.lineColor,
                        index = entryIndex,
                    )
                }
            }

            if (component.hasLineBackgroundShader) {
                lineBackgroundPath.lineTo(prevX, bounds.bottom)
                lineBackgroundPath.close()
                component.drawBackgroundLine(context, bounds, lineBackgroundPath)
            }
            component.drawLine(context, linePath)

            drawPointsAndDataLabels(
                lineSpec = component,
                entries = entries,
                drawingStart = drawingStart,
            )
        }
    }

    /**
     * Draws a line’s points ([LineSpec.point]) and their corresponding data labels ([LineSpec.dataLabel]).
     */
    protected open fun ChartDrawContext.drawPointsAndDataLabels(
        lineSpec: LineSpec,
        entries: List<ChartEntry>,
        drawingStart: Float,
    ) {
        if (lineSpec.point == null && lineSpec.dataLabel == null) return

        forEachPointWithinBoundsIndexed(
            entries = entries,
            segment = segmentProperties,
            drawingStart = drawingStart,
        ) { index, chartEntry, x, y ->

            if (lineSpec.point != null) lineSpec.drawPoint(context = this, x = x, y = y)

            lineSpec.dataLabel.takeIf { pointPosition.dataLabelsToSkip <= index }?.let { textComponent ->

                val distanceFromLine = maxOf(
                    a = lineSpec.lineThicknessDp,
                    b = lineSpec.pointSizeDpOrZero,
                ).half.pixels

                val text = lineSpec.dataLabelValueFormatter.formatValue(
                    value = chartEntry.y,
                    chartValues = chartValuesManager.getChartValues(axisPosition = targetVerticalAxisPosition),
                )
                val verticalPosition = lineSpec.dataLabelVerticalPosition.inBounds(
                    bounds = bounds,
                    distanceFromPoint = distanceFromLine,
                    componentHeight = 0f, // textComponent.getHeight(
//                        context = this,
//                        text = text,
//                        width = segmentWidth,
//                        rotationDegrees = lineSpec.dataLabelRotationDegrees,
//                    ),
                    y = y,
                )
                val dataLabelY = y + when (verticalPosition) {
                    VerticalPosition.Top -> -distanceFromLine
                    VerticalPosition.Center -> 0f
                    VerticalPosition.Bottom -> distanceFromLine
                }
                textComponent.drawText(
                    context = this,
                    textX = x,
                    textY = dataLabelY,
                    text = text,
                    verticalPosition = verticalPosition,
                    maxTextWidth = segmentWidth,
                    rotationDegrees = lineSpec.dataLabelRotationDegrees,
                )
            }
        }
    }

    /**
     * Clears the temporary data saved during a single [drawChart] run.
     */
    protected fun resetTempData() {
        entryLocationMap.clear()
        linePath.reset()
        lineBackgroundPath.reset()
//        linePath.rewind()
//        lineBackgroundPath.rewind()
    }

    /**
     * Performs the given [action] for each [ChartEntry] in [entries] that lies within the chart’s bounds.
     */
    protected open fun DrawContext.forEachPointWithinBoundsIndexed(
        entries: List<ChartEntry>,
        segment: SegmentProperties,
        drawingStart: Float,
        action: (index: Int, entry: ChartEntry, x: Float, y: Float) -> Unit,
    ) {
        val chartValues = chartValuesManager.getChartValues(targetVerticalAxisPosition)

        val minX = chartValues.minX
        val maxX = chartValues.maxX
        val minY = chartValues.minY
        val maxY = chartValues.maxY
        val xStep = chartValues.xStep

        var x: Float
        var y: Float

        var prevEntry: ChartEntry? = null
        var lastEntry: ChartEntry? = null

        val heightMultiplier: Float = bounds.height / (maxY - minY)

        val boundsStart: Float = bounds.getStart(isLtr = isLtr)
        val boundsEnd: Float = boundsStart + layoutDirectionMultiplier * bounds.width

        fun getDrawX(entry: ChartEntry): Float = drawingStart + layoutDirectionMultiplier *
            (segment.cellWidth + segment.marginWidth) * (entry.x - minX) / xStep

        fun getDrawY(entry: ChartEntry): Float =
            bounds.bottom - (entry.y - minY) * heightMultiplier

        entries.forEachInIndexed(minX - xStep..maxX + xStep) { index, entry ->

            x = getDrawX(entry)
            y = getDrawY(entry)

            when {
                isLtr && x < boundsStart || isLtr.not() && x > boundsStart -> {
                    prevEntry = entry
                }

                x in boundsStart.rangeWith(other = boundsEnd) -> {
                    prevEntry?.also {
                        action(index, it, getDrawX(it), getDrawY(it))
                        prevEntry = null
                    }
                    action(index, entry, x, y)
                }

                (isLtr && x > boundsEnd || isLtr.not() && x < boundsEnd) && lastEntry == null -> {
                    action(index, entry, x, y)
                    lastEntry = entry
                }
            }
        }
    }

    override fun getSegmentProperties(
        context: MeasureContext,
        model: ChartEntryModel,
    ): SegmentProperties = with(context) {
        segmentProperties.set(
            cellWidth = lines.maxOf { it.pointSizeDp.pixels },
            marginWidth = spacingDp.pixels,
            labelPosition = pointPosition.labelPosition,
        )
    }

    override fun updateChartValues(
        chartValuesManager: ChartValuesManager,
        model: ChartEntryModel,
    ) {
        @Suppress("DEPRECATION")
        chartValuesManager.tryUpdate(
            minX = axisValuesOverrider?.getMinX(model) ?: minX ?: model.minX,
            maxX = axisValuesOverrider?.getMaxX(model) ?: maxX ?: model.maxX,
            minY = axisValuesOverrider?.getMinY(model) ?: minY ?: min(model.minY, 0f),
            maxY = axisValuesOverrider?.getMaxY(model) ?: maxY ?: model.maxY,
            chartEntryModel = model,
            axisPosition = targetVerticalAxisPosition,
        )
    }

    override fun getInsets(
        context: MeasureContext,
        outInsets: Insets,
        segmentProperties: SegmentProperties,
    ): Unit = with(context) {
        outInsets.setVertical(
            value = lines.maxOf {
                if (it.point != null) max(a = it.lineThicknessDp, b = it.pointSizeDp) else it.lineThicknessDp
            }.pixels,
        )
    }

    /**
     * Defines the horizontal position of each of a line chart’s points in its corresponding segment.
     */
    public enum class PointPosition(
        internal val labelPosition: HorizontalAxis.LabelPosition,
        internal val dataLabelsToSkip: Int,
    ) {
        Start(
            labelPosition = HorizontalAxis.LabelPosition.Start,
            dataLabelsToSkip = 1,
        ),
        Center(
            labelPosition = HorizontalAxis.LabelPosition.Center,
            dataLabelsToSkip = 0,
        ),
    }
}
