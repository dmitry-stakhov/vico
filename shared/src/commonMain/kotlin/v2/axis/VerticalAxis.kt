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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.orZero
import v2.formatter.IntFormatter

public class VerticalAxis<Position : AxisPosition.Vertical>(
    public val position: Position,
    public val label: @Composable (label: String) -> Unit,
) {
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

    internal fun Placeable.PlacementScope.placeAxis(
        axisLine: Placeable,
        axisOffset: Int,
        axisLabelPlaceables: List<Placeable>,
        tickPlaceables: List<Placeable>,
        constraints: Constraints
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
}

internal data class AxisPlaceables(
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

