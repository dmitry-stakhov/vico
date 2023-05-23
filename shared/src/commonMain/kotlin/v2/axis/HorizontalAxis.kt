package v2.axis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.half

public class HorizontalAxis<Position : AxisPosition.Horizontal>(
    public override val position: Position,
    public override val label: @Composable (label: String) -> Unit,
) : Axis<Position>() {
    internal fun SubcomposeMeasureScope.getAxisLinePlaceable(
        constraints: Constraints,
    ): Placeable {
        return subcompose("line") { Box(Modifier.fillMaxWidth().height(1.dp).background(Color.Black)) }.first().measure(constraints)
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
        measureContext: MeasureContext
    ): AxisPlaceables {
        return with(measureScope) {
            val constraints = Constraints(maxWidth = bounds.width.toInt(), maxHeight = bounds.height.toInt())
            val line = getAxisLinePlaceable(constraints)
            val labels = getAxisPlaceables(constraints, measureContext, label)
            val ticks = getTickPlaceables(labels.size)
            AxisPlaceables(line, labels, ticks)
        }
    }

    override fun Placeable.PlacementScope.placeAxis(
        axisLine: Placeable,
        axisOffset: Int,
        axisLabelPlaceables: List<Placeable>,
        tickPlaceables: List<Placeable>,
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
    label: @Composable (label: String) -> Unit = { Text(it) },
): HorizontalAxis<AxisPosition.Horizontal.Top> = remember(label) {
    HorizontalAxis(AxisPosition.Horizontal.Top, label)
}

@Composable
public fun bottomAxis(
    label: @Composable (label: String) -> Unit = { Text(it) },
): HorizontalAxis<AxisPosition.Horizontal.Bottom> = remember(label) {
    HorizontalAxis(AxisPosition.Horizontal.Bottom, label)
}
