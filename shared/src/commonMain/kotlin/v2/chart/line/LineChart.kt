package v2.chart.line

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DefaultAlpha
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.decoration.Decoration
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.segmentWidth
import com.patrykandpatrick.vico.core.chart.forEachInIndexed
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec
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
import com.patrykandpatrick.vico.core.component.text.inBounds
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
import extension.isLtr
import extension.layoutDirectionMultiplier
import v2.chart.BaseChart
import kotlin.math.max
import kotlin.math.min

public open class LineChart(
    public var lines: List<LineChart.LineSpec> = listOf(LineChart.LineSpec()),
    public var spacing: Dp = DefaultDimens.POINT_SPACING.dp,
    public var targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    public var pointPosition: LineChart.PointPosition = LineChart.PointPosition.Center,
) : BaseChart<ChartEntryModel>() {
    /**
     * The [Path] used to draw the lines, each of which corresponds to a [LineSpec].
     */
    protected val linePath: Path = Path()

    /**
     * The [Path] used to draw the backgrounds of the lines, each of which corresponds to a [LineSpec].
     */
    protected val lineBackgroundPath: Path = Path()

    override fun drawChart(
        drawScope: DrawScope,
        context: ChartDrawContext,
        model: ChartEntryModel
    ) {
        resetTempData()

        val (cellWidth, spacing) = segmentProperties

        model.entries.forEachIndexed { entryListIndex, entries ->

            linePath.reset()
//            linePath.rewind()
            lineBackgroundPath.reset()
//            lineBackgroundPath.rewind()
            val component = lines.getRepeating(entryListIndex)

            var prevX = bounds.getStart(isLtr = drawScope.isLtr)
            var prevY = bounds.bottom

            val drawingStartAlignmentCorrection = drawScope.layoutDirectionMultiplier *
                    when (pointPosition) {
                        LineChart.PointPosition.Start -> 0f
                        LineChart.PointPosition.Center -> (spacing + cellWidth).half
                    }

            val drawingStart =
                bounds.getStart(isLtr = drawScope.isLtr) + drawingStartAlignmentCorrection - context.horizontalScroll

            forEachPointWithinBoundsIndexed(
                chartValuesManager = context.chartValuesManager,
                drawScope = drawScope,
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
                component.drawBackgroundLine(context.drawScope, bounds, lineBackgroundPath)
            }
            component.drawLine(context.drawScope, linePath)

            context.drawPointsAndDataLabels(
                drawScope = drawScope,
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
        drawScope: DrawScope,
        lineSpec: LineSpec,
        entries: List<ChartEntry>,
        drawingStart: Float,
    ) {
        if (lineSpec.point == null && lineSpec.dataLabel == null) return

        forEachPointWithinBoundsIndexed(
            chartValuesManager = this.chartValuesManager,
            drawScope = drawScope,
            entries = entries,
            segment = segmentProperties,
            drawingStart = drawingStart,
        ) { index, chartEntry, x, y ->

            if (lineSpec.point != null) lineSpec.drawPoint(drawScope = drawScope, x = x, y = y)

            lineSpec.dataLabel.takeIf { pointPosition.dataLabelsToSkip <= index }
                ?.let { textComponent ->

                    val distanceFromLine = with(drawScope) {
                        maxOf(
                            a = lineSpec.lineThickness.value,
                            b = lineSpec.pointSizeDpOrZero,
                        ).half.dp.toPx()
                    }

                    val text = lineSpec.dataLabelValueFormatter.formatValue(
                        value = chartEntry.y,
                        chartValues = chartValuesManager.getChartValues(axisPosition = targetVerticalAxisPosition),
                    )
                    val verticalPosition = lineSpec.dataLabelVerticalPosition.inBounds(
                        bounds = bounds,
                        distanceFromPoint = distanceFromLine,
                        componentHeight = textComponent.getHeight(
                            extras = this,
                            density = Density(density),
                            text = text,
                            width = segmentWidth(drawScope),
                            rotationDegrees = lineSpec.dataLabelRotationDegrees,
                        ),
                        y = y,
                    )
                    val dataLabelY = y + when (verticalPosition) {
                        Alignment.Top -> -distanceFromLine
                        Alignment.Center -> 0f
                        Alignment.Bottom -> distanceFromLine
                        else -> error("")
                    }
                    textComponent.drawText(
                        drawScope = drawScope,
                        extras = this,
                        textX = x,
                        textY = dataLabelY,
                        text = text,
                        verticalPosition = verticalPosition,
                        maxTextWidth = segmentWidth(drawScope),
                        rotationDegrees = lineSpec.dataLabelRotationDegrees,
                    )
                }
        }
    }

    /**
     * Performs the given [action] for each [ChartEntry] in [entries] that lies within the chart’s bounds.
     */
    protected open fun forEachPointWithinBoundsIndexed(
        chartValuesManager: ChartValuesManager,
        drawScope: DrawScope,
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

        val boundsStart: Float = bounds.getStart(isLtr = drawScope.isLtr)
        val boundsEnd: Float = boundsStart + drawScope.layoutDirectionMultiplier * bounds.width

        fun getDrawX(entry: ChartEntry): Float =
            drawingStart + drawScope.layoutDirectionMultiplier *
                    (segment.cellWidth + segment.marginWidth) * (entry.x - minX) / xStep

        fun getDrawY(entry: ChartEntry): Float =
            bounds.bottom - (entry.y - minY) * heightMultiplier

        entries.forEachInIndexed(minX - xStep..maxX + xStep) { index, entry ->

            x = getDrawX(entry)
            y = getDrawY(entry)

            when {
                drawScope.isLtr && x < boundsStart || drawScope.isLtr.not() && x > boundsStart -> {
                    prevEntry = entry
                }

                x in boundsStart.rangeWith(other = boundsEnd) -> {
                    prevEntry?.also {
                        action(index, it, getDrawX(it), getDrawY(it))
                        prevEntry = null
                    }
                    action(index, entry, x, y)
                }

                (drawScope.isLtr && x > boundsEnd || drawScope.isLtr.not() && x < boundsEnd) && lastEntry == null -> {
                    action(index, entry, x, y)
                    lastEntry = entry
                }
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
     * The chart’s [MutableSegmentProperties] instance, which holds information about the segment properties.
     */
    protected val segmentProperties: MutableSegmentProperties = MutableSegmentProperties()

    override val entryLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()

    override fun getSegmentProperties(
        density: Density,
        model: ChartEntryModel,
    ): SegmentProperties = with(density) {
        segmentProperties.set(
            cellWidth = lines.maxOf { it.pointSize.toPx() },
            marginWidth = spacing.toPx(),
            labelPosition = pointPosition.labelPosition,
        )
    }

    override fun updateChartValues(
        chartValuesManager: ChartValuesManager,
        model: ChartEntryModel,
    ) {
        chartValuesManager.tryUpdate(
            minX = axisValuesOverrider?.getMinX(model) ?: model.minX,
            maxX = axisValuesOverrider?.getMaxX(model) ?: model.maxX,
            minY = axisValuesOverrider?.getMinY(model) ?: min(model.minY, 0f),
            maxY = axisValuesOverrider?.getMaxY(model) ?: model.maxY,
            chartEntryModel = model,
            axisPosition = targetVerticalAxisPosition,
        )
    }

    override fun getInsets(
        density: Density,
        context: MeasureContext,
        outInsets: Insets,
        segmentProperties: SegmentProperties,
    ): Unit = with(density) {
        outInsets.setVertical(
            value = lines.maxOf {
                if (it.point != null) max(
                    a = it.lineThickness.value,
                    b = it.pointSize.value
                ) else it.lineThickness.value
            }.dp.toPx(),
        )
    }
}

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
): v2.chart.line.LineChart = remember { v2.chart.line.LineChart() }.apply {
    this.lines = lines
    this.spacing = spacing
    this.pointPosition = pointPosition
    this.axisValuesOverrider = axisValuesOverrider
    this.targetVerticalAxisPosition = targetVerticalAxisPosition
//    decorations?.also(::setDecorations)
//    persistentMarkers?.also(::setPersistentMarkers)
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
    dataLabelVerticalPosition: Alignment.Vertical = Alignment.Top,
    dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
    dataLabelRotationDegrees: Float = 0f,
    pointConnector: LineSpec.PointConnector = DefaultPointConnector(),
): LineSpec = LineSpec(
    lineColor = lineColor.toArgb(),
    lineThickness = lineThickness,
    lineBackgroundShader = lineBackgroundShader,
    lineCap = lineCap,
    point = point,
    pointSize = pointSize,
    dataLabel = dataLabel,
    dataLabelVerticalPosition = dataLabelVerticalPosition,
    dataLabelValueFormatter = dataLabelValueFormatter,
    dataLabelRotationDegrees = dataLabelRotationDegrees,
    pointConnector = pointConnector,
)
