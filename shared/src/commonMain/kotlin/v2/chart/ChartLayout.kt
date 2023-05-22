package v2.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import com.patrykandpatrick.vico.compose.chart.rememberScrollListener
import com.patrykandpatrick.vico.compose.chart.scroll.ChartScrollState
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.layout.getMeasureContext
import com.patrykandpatrick.vico.core.axis.AxisManager
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.model.Point
import v2.axis.AxisPlaceables
import v2.axis.HorizontalAxis
import v2.axis.VerticalAxis

@Composable
public fun <Model : ChartEntryModel> ChartLayout(
    chart: Chart<Model>,
    topAxis: HorizontalAxis<AxisPosition.Horizontal.Top>? = null,
    bottomAxis: HorizontalAxis<AxisPosition.Horizontal.Bottom>? = null,
    startAxis: VerticalAxis<AxisPosition.Vertical.Start>? = null,
    endAxis: VerticalAxis<AxisPosition.Vertical.End>? = null,
    model: Model,
    modifier: Modifier = Modifier,
    chartScrollState: ChartScrollState = rememberChartScrollState(),
) {
    val axisManager = remember { AxisManager() }
    var bounds = remember { Rect.Zero }
    val markerTouchPoint = remember { mutableStateOf<Point?>(null) }
    val zoom = remember { mutableStateOf(1f) }
    val measureContext = getMeasureContext(false, zoom.value, bounds)
    val interactionSource = remember { MutableInteractionSource() }
    val interaction = interactionSource.interactions.collectAsState(initial = null)
    val scrollListener = rememberScrollListener(markerTouchPoint, interaction)
    val lastMarkerEntryModels = remember { mutableStateOf(emptyList<Marker.EntryModel>()) }

    chartScrollState.registerScrollListener(scrollListener)
    chart.updateChartValues(measureContext.chartValuesManager, model)

    SubcomposeLayout(modifier = modifier) { constraints ->

        val startAxisPlaceables = startAxis?.run {
            val line = getAxisLinePlaceable(constraints)
            val labels = getAxisPlaceables(constraints, measureContext, label)
            val ticks = getTickPlaceables(labels.size)
            AxisPlaceables(line, labels, ticks)
        }

        val endAxisPlaceables = endAxis?.run {
            val line = getAxisLinePlaceable(constraints)
            val labels = getAxisPlaceables(constraints, measureContext, label)
            val ticks = getTickPlaceables(labels.size)
            AxisPlaceables(line, labels, ticks)
        }

        val chartPlaceable = subcompose("chart", { Box(Modifier.fillMaxSize().background(Color.Red.copy(alpha = 0.5f))) }).map {
            val startOffset = startAxisPlaceables?.labels?.maxOf { it.width }.orZero
            val endOffset = endAxisPlaceables?.labels?.maxOf { it.width }.orZero
            it.measure(constraints.copy(maxWidth = constraints.maxWidth - startOffset - endOffset))
        }.first()

        layout(constraints.maxWidth, constraints.maxHeight) {
            startAxisPlaceables?.run {
                val axisWidth = startAxisPlaceables.labels.maxOf { it.width }
                with(startAxis) {
                    placeAxis(
                        axisLine = startAxisPlaceables.axis,
                        axisOffset = axisWidth,
                        axisLabelPlaceables = startAxisPlaceables.labels,
                        tickPlaceables = startAxisPlaceables.ticks,
                        constraints = constraints
                    )
                }
            }
            endAxisPlaceables?.run {
                val axisWidth = endAxisPlaceables.labels.maxOf { it.width }
                with(endAxis) {
                    placeAxis(
                        axisLine = endAxisPlaceables.axis,
                        axisOffset = axisWidth,
                        axisLabelPlaceables = endAxisPlaceables.labels,
                        tickPlaceables = endAxisPlaceables.ticks,
                        constraints = constraints
                    )
                }
            }

            val chartOffset = startAxisPlaceables?.labels?.maxOf { it.width }.orZero
            chartPlaceable.place(chartOffset, 0)
        }
    }
}
