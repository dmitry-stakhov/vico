package v2.axis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.HorizontalInsets
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.context.Extras
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.getEnd
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.orZero
import extension.isLtr
import v2.formatter.IntFormatter
import kotlin.math.roundToInt

private const val LABELS_KEY = "labels"
private const val TITLE_ABS_ROTATION_DEGREES = 90f

public class VerticalAxis<Position : AxisPosition.Vertical>(
    public override val position: Position,
    public override val label: @Composable (label: String) -> Unit,
): Axis<Position>() {
    /**
     * Defines the horizontal position of each axis label relative to the axis line.
     */
    public var horizontalLabelPosition: HorizontalLabelPosition = HorizontalLabelPosition.Outside

    /**
     * Defines the vertical position of each axis label relative to its corresponding tick.
     */
    public var verticalLabelPosition: VerticalLabelPosition = VerticalLabelPosition.Center

    override fun getHorizontalInsets(
        subcomposeMeasureScope: SubcomposeMeasureScope,
        context: MeasureContext,
        availableHeight: Float,
        outInsets: HorizontalInsets,
    ): Unit = with(context) {
        val labels = getLabels(maxLabelCount = getDrawLabelCount(subcomposeMeasureScope, availableHeight.toInt()))

        val desiredWidth = getDesiredWidth(subcomposeMeasureScope, context, labels)

        outInsets.set(
            start = if (position.isStart) desiredWidth else 0f,
            end = if (position.isEnd) desiredWidth else 0f,
        )
    }

    /**
     * Calculates the optimal width for this [VerticalAxis], accounting for the value of [sizeConstraint].
     */
    private fun getDesiredWidth(
        subcomposeMeasureScope: SubcomposeMeasureScope,
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
                    (getMaxLabelWidth(
                        subcomposeMeasureScope = subcomposeMeasureScope,
                        labels = labels
                    ) + titleComponentWidth + axisThickness.half + tickLengthPx)
                        .coerceIn(
                            minimumValue = constraint.minSize.toPx(),
                            maximumValue = constraint.maxSize.toPx()
                        )
                }
            }

            is SizeConstraint.Exact -> with(Density(density)) { constraint.size.toPx() }
            is SizeConstraint.Fraction -> canvasBounds.width * constraint.fraction
            is SizeConstraint.TextWidth -> {
                val labelWidth = subcomposeMeasureScope.subcompose("label-getDesiredWidth-$position") {
                    label(constraint.text)
                }.first().measure(Constraints()).width
                labelWidth + with(Density(density)) { tickLengthPx + axisThickness.half }
            }
        }
    }

    private fun MeasureContext.getMaxLabelWidth(
        subcomposeMeasureScope: SubcomposeMeasureScope,
        labels: List<CharSequence>
    ): Float =
        when (horizontalLabelPosition) {
            HorizontalLabelPosition.Outside -> {
                labels.maxOfOrNull {
                    subcomposeMeasureScope.subcompose("label-getMaxLabelWidth-$it-$position") {
                        label(it.toString())
                    }.first().measure(Constraints()).width.toFloat()
                }.orZero
            }

            HorizontalLabelPosition.Inside -> 0f
        }

    private fun MeasureContext.getDrawLabelCount(
        subcomposeMeasureScope: SubcomposeMeasureScope,
        availableHeight: Int
    ): Int {

        val chartValues = chartValuesManager.getChartValues(position)

        fun getLabelHeight(value: Float): Float =
            subcomposeMeasureScope.subcompose("getDrawLabelCount-$value-$position") { label(value.roundToInt().toString())}.first().measure(
                Constraints()
            ).height.toFloat()

        val avgHeight = arrayOf(
            getLabelHeight(chartValues.minY),
            getLabelHeight((chartValues.maxY + chartValues.minY) / 2),
            getLabelHeight(chartValues.maxY),
        ).maxOrNull().orZero

        return (availableHeight / avgHeight + 1).toInt().coerceAtMost(maxLabelCount)
    }

    override fun getInsets(
        subcomposeMeasureScope: SubcomposeMeasureScope,
        density: Density,
        context: MeasureContext,
        outInsets: Insets,
        segmentProperties: SegmentProperties,
    ): Unit = with(context) {
        val labelHeight = subcomposeMeasureScope.subcompose("getInsets-$position") {
            label("0")
        }.first().measure(Constraints()).height.toFloat()
        val lineThickness = maxOf(density.axisThickness, density.tickThickness)
        when (verticalLabelPosition) {
            VerticalLabelPosition.Center -> outInsets.set(
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

    override fun getPlaceables(measureScope: SubcomposeMeasureScope, measureContext: MeasureContext) : AxisPlaceables {
        return with(measureScope) {
            val constraints = Constraints(maxWidth = bounds.width.toInt(), maxHeight = bounds.height.toInt())
            val line = getAxisLinePlaceable(constraints)
            val labels = getAxisPlaceables(constraints, measureContext, label)
            val ticks = getTickPlaceables(labels.size)
            AxisPlaceables(line, labels, ticks)
        }
    }

    internal fun SubcomposeMeasureScope.getAxisLinePlaceable(
        constraints: Constraints,
    ): Placeable {
        return subcompose("line-$position") { Box(Modifier.fillMaxHeight().width(1.dp).background(Color.Black)) }.first().measure(constraints)
    }

    internal fun SubcomposeMeasureScope.getAxisPlaceables(
        constraints: Constraints,
        measureContext: MeasureContext,
        label: @Composable (label: String) -> Unit
    ): List<Placeable> {
        val ticksCount = getDrawLabelCount(constraints.maxHeight, measureContext)
        val labels = measureContext.getLabels(ticksCount)

        return subcompose("labels-$position") { labels.forEach { label(it.toString()) } }.map {
            it.measure(Constraints())
        }
    }

    override fun Placeable.PlacementScope.placeAxis(
        axisLine: Placeable,
        axisOffset: Int,
        axisLabelPlaceables: List<Placeable>,
        tickPlaceables: List<Placeable>,
        constraints: Constraints,
    ) {
        val axisLineOffset = if (position.isLeft(true)) {
            axisOffset - axisLine.width
        } else {
            constraints.maxWidth - (axisOffset - axisLine.width)
        }
        axisLine.place(axisLineOffset, 0)

        val axisStep = constraints.maxHeight / (axisLabelPlaceables.size - 1)
        var y = constraints.maxHeight
        axisLabelPlaceables.forEach {
            val x = if (position.isLeft(true)) {
                0
            } else {
                constraints.maxWidth - it.width
            }
            it.place(x, y - it.height, 2f)
            y -= axisStep
        }

        var y2 = constraints.maxHeight
        val axisOffset = axisLabelPlaceables.maxOf { it.width }
        tickPlaceables.forEach {
            val x = if (position.isLeft(true)) {
                axisOffset - it.width.half
            } else {
                constraints.maxWidth - axisOffset - it.width.half
            }
            it.place(x, y2 - it.height, 2f)
            y2 -= axisStep
        }
    }

    internal fun SubcomposeMeasureScope.getTickPlaceables(count: Int): List<Placeable> {
        return subcompose("Tick-$position") {
            repeat(count)  { Divider(Modifier.size(10.dp), color = Color.Black) }
        }.map {
            it.measure(Constraints())
        }
    }

    private fun SubcomposeMeasureScope.getDrawLabelCount(
        availableSpace: Int,
        measureContext: MeasureContext,
    ): Int {
        val chartValues = measureContext.chartValuesManager.getChartValues(position)

        val minSpace = subcompose("min-label-$position") {
            label(IntFormatter.format(chartValues.minY))
        }.first().measure(Constraints())
        val middleSpace = subcompose("middle-label-$position") {
            label(IntFormatter.format((chartValues.maxY + chartValues.minY) / 2))
        }.first().measure(Constraints())
        val maxSpace = subcompose("max-label-$position") {
            label(IntFormatter.format(chartValues.maxY))
        }.first().measure(Constraints())

        val avgSpace = when (position) {
            is AxisPosition.Horizontal -> {
                arrayOf(minSpace.width, middleSpace.width, maxSpace.width).maxOrNull().orZero
            }
            else -> {
                arrayOf(minSpace.height, middleSpace.height, maxSpace.height).maxOrNull().orZero
            }
        }

        return (availableSpace / avgSpace + 1).coerceAtMost(maxLabelCount)
    }

    private fun MeasureContext.getLabels(
        maxLabelCount: Int = 100,
    ): List<CharSequence> {
        val chartValues = chartValuesManager.getChartValues(AxisPosition.Vertical.Start)
        val step = (chartValues.maxY - chartValues.minY) / (maxLabelCount - 1)
        val labels = mutableListOf<CharSequence>()
        for (index in 0 until maxLabelCount) {
            val value = chartValues.minY + step * index
            labels += value.toInt().toString()
        }
        return labels
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
     * @see Alignment
     */
    public enum class VerticalLabelPosition(public val textPosition: Alignment.Vertical) {
        Center(Alignment.CenterVertically),
        Top(Alignment.Top),
        Bottom(Alignment.Bottom),
    }
}

public data class AxisPlaceables(
    val axis: Placeable,
    val labels: List<Placeable>,
    val ticks: List<Placeable>
)

@Composable
public fun startAxis(
    label: @Composable (label: String) -> Unit = { Text(it) },
): VerticalAxis<AxisPosition.Vertical.Start> = remember(label) {
    VerticalAxis(AxisPosition.Vertical.Start, label)
}

@Composable
public fun endAxis(
    label: @Composable (label: String) -> Unit = { Text(it) },
): VerticalAxis<AxisPosition.Vertical.End> = remember(label) {
    VerticalAxis(AxisPosition.Vertical.End, label)
}

internal const val maxLabelCount = 100

