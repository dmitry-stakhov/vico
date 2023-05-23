package v2.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.patrykandpatrick.vico.compose.chart.rememberScrollListener
import com.patrykandpatrick.vico.compose.chart.rememberZoomState
import com.patrykandpatrick.vico.compose.chart.scroll.ChartScrollState
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.extension.chartTouchEvent
import com.patrykandpatrick.vico.compose.layout.getMeasureContext
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.model.Point
import kotlinx.coroutines.launch
import v2.axis.AxisManager
import v2.axis.AxisPlaceables
import v2.axis.HorizontalAxis
import v2.axis.VerticalAxis
import v2.core.layout.VirtualLayout

@Composable
public fun <Model : ChartEntryModel> ChartLayout(
    chart: Chart<Model>,
    startAxis: VerticalAxis<AxisPosition.Vertical.Start>? = null,
    topAxis: HorizontalAxis<AxisPosition.Horizontal.Top>? = null,
    endAxis: VerticalAxis<AxisPosition.Vertical.End>? = null,
    bottomAxis: HorizontalAxis<AxisPosition.Horizontal.Bottom>? = null,
    model: Model,
    modifier: Modifier = Modifier,
    chartScrollState: ChartScrollState = rememberChartScrollState(),
) {
    val axisManager = remember(startAxis, topAxis, endAxis, bottomAxis) {
        AxisManager(startAxis, topAxis, endAxis, bottomAxis)
    }
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

    val virtualLayout = remember(axisManager) { VirtualLayout(axisManager) }
    val elevationOverlayColor = currentChartStyle.elevationOverlayColor.toArgb()
    val (wasMarkerVisible, setWasMarkerVisible) = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val onZoom = rememberZoomState(
        zoom = zoom,
        getScroll = { chartScrollState.value },
        scrollBy = { value -> coroutineScope.launch { chartScrollState.scrollBy(value) } },
        chartBounds = chart.bounds,
    )

//    LaunchedEffect(key1 = model.id) {
//        chartScrollSpec.performAutoScroll(
//            model = model,
//            oldModel = oldModel,
//            chartScrollState = chartScrollState,
//        )
//    }

    val marker = null
    SubcomposeLayout(modifier = Modifier
        .fillMaxSize()
        .chartTouchEvent(
            setTouchPoint = remember(marker) {
                markerTouchPoint
                    .component2()
                    .takeIf { marker != null }
            },
            isScrollEnabled = true,
            scrollableState = chartScrollState,
            onZoom = onZoom.takeIf { true },
            interactionSource = interactionSource,
        ),
    ) { constraints ->
        val size = IntSize(constraints.maxWidth, constraints.maxHeight).toSize()
        bounds = size.toRect()
        chart.updateChartValues(measureContext.chartValuesManager, model)

        val segmentProperties = chart.getSegmentProperties(this, model)

//        val chartBounds = virtualLayout.setBounds(
//            drawScope = this,
//            context = measureContext,
//            contentBounds = bounds,
//            chart = chart,
//            legend = legend,
//            segmentProperties = segmentProperties,
//            marker,
//        )

        val startAxisPlaceables = startAxis?.run {
            val line = getAxisLinePlaceable(constraints)
            val labels = getAxisPlaceables(constraints, measureContext, labelLayout)
            val ticks = getTickPlaceables(labels.size)
            AxisPlaceables(line, labels, ticks)
        }

        val endAxisPlaceables = endAxis?.run {
            val line = getAxisLinePlaceable(constraints)
            val labels = getAxisPlaceables(constraints, measureContext, labelLayout)
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
                        constraints = constraints,
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
