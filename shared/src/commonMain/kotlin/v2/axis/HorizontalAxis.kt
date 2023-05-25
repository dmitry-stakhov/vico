package v2.axis

import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.half

public class HorizontalAxis<Position : AxisPosition.Horizontal>(
    override val position: Position,
    override val label: @Composable (label: String) -> Unit,
    override val tick: @Composable () -> Unit,
    override val axisLine: @Composable () -> Unit,
    override val guideline: @Composable () -> Unit
) : Axis<Position>() {
    internal fun SubcomposeMeasureScope.getAxisLinePlaceable(constraints: Constraints): Placeable {
        return subcompose("line", axisLine).first().measure(constraints)
    }

    internal fun SubcomposeMeasureScope.getAxisPlaceables(
        constraints: Constraints,
        measureContext: MeasureContext,
        label: @Composable (label: String) -> Unit
    ): List<Placeable> {
        val labelPlaceable = subcompose("single-label") { label("99") }.first().measure(Constraints())

        val ticksCount = getDrawLabelCount(constraints.maxWidth, labelPlaceable.width)
        val labels = measureContext.getLabels(ticksCount)

        return subcompose("labels") { labels.forEach { label(it.toString()) } }.map {
            it.measure(Constraints())
        }
    }

    override fun getPlaceables(
        measureScope: SubcomposeMeasureScope,
        measureContext: MeasureContext,
        chartSize: IntSize
    ): AxisPlaceables {
        return with(measureScope) {
            val constraints = Constraints(maxWidth = bounds.width.toInt(), maxHeight = bounds.height.toInt())
            val line = getAxisLinePlaceable(constraints)
            val labels = getAxisPlaceables(constraints, measureContext, label)
            val ticks = getTickPlaceables(labels.size)
            AxisPlaceables(line, labels, ticks, emptyList())
        }
    }

    override fun Placeable.PlacementScope.placeAxis(
        layoutDirection: LayoutDirection,
        axisLine: Placeable,
        axisOffset: Int,
        axisLabelPlaceables: List<Placeable>,
        tickPlaceables: List<Placeable>,
        guidelinePlaceables: List<Placeable>,
        constraints: Constraints,
    ) {
        axisLine.place(axisOffset - axisLine.width, 0, 1f)

        val axisStep = constraints.maxHeight / (axisLabelPlaceables.size - 1)
        var y = constraints.maxHeight
        axisLabelPlaceables.forEach {
            it.place(0, y - it.height, 1f)
            y -= axisStep
        }

        var y2 = constraints.maxHeight
        val axisOffset = axisLabelPlaceables.maxOf { it.width }
        tickPlaceables.forEach {
            it.place(axisOffset - it.width.half, y2 - it.height, 2f)
            y2 -= axisStep
        }
    }

    internal fun SubcomposeMeasureScope.getTickPlaceables(count: Int): List<Placeable> {
        return subcompose("Tick") {
            repeat(count)  { Divider(Modifier.size(10.dp), color = Color.Black) }
        }.map {
            it.measure(Constraints())
        }
    }

    private fun getDrawLabelCount(availableHeight: Int, labelHeight: Int): Int {
        return (availableHeight / labelHeight + 1).coerceAtMost(maxLabelCount)
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

@Composable
public fun topAxis(
    label: @Composable (label: String) -> Unit = { AxisLabel(it) },
    tick: @Composable () -> Unit = { HorizontalAxisTick() },
    axisLine: @Composable () -> Unit = { HorizontalAxisLine() },
    guideline: @Composable () -> Unit = { HorizontalAxisGuideline() },
): HorizontalAxis<AxisPosition.Horizontal.Top> = remember(label, tick, axisLine, guideline) {
    HorizontalAxis(AxisPosition.Horizontal.Top, label, tick, axisLine, guideline)
}

@Composable
public fun bottomAxis(
    label: @Composable (label: String) -> Unit = { AxisLabel(it) },
    tick: @Composable () -> Unit = { HorizontalAxisTick() },
    axisLine: @Composable () -> Unit = { HorizontalAxisLine() },
    guideline: @Composable () -> Unit = { HorizontalAxisGuideline() },
): HorizontalAxis<AxisPosition.Horizontal.Bottom> = remember(label, tick, axisLine, guideline) {
    HorizontalAxis(AxisPosition.Horizontal.Bottom, label, tick, axisLine, guideline)
}
