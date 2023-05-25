package v2.chart

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.roundToIntRect
import androidx.compose.ui.unit.toSize
import com.patrykandpatrick.vico.compose.chart.ChartBox
import com.patrykandpatrick.vico.compose.chart.entry.collectAsState
import com.patrykandpatrick.vico.compose.chart.entry.defaultDiffAnimationSpec
import com.patrykandpatrick.vico.compose.chart.rememberScrollListener
import com.patrykandpatrick.vico.compose.chart.rememberZoomState
import com.patrykandpatrick.vico.compose.chart.scroll.ChartScrollSpec
import com.patrykandpatrick.vico.compose.chart.scroll.ChartScrollState
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.extension.chartTouchEvent
import com.patrykandpatrick.vico.compose.layout.getMeasureContext
import com.patrykandpatrick.vico.compose.state.MutableSharedState
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.draw.chartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.drawMarker
import com.patrykandpatrick.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatrick.vico.core.chart.edges.FadingEdges
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartModelProducer
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.legend.Legend
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerVisibilityChangeListener
import com.patrykandpatrick.vico.core.model.Point
import kotlinx.coroutines.launch
import v2.axis.AxisManager
import v2.axis.AxisPlaceables
import v2.axis.AxisRenderer
import v2.axis.HorizontalAxis
import v2.axis.VerticalAxis
import v2.core.layout.VirtualLayout

/**
 * Displays a chart.
 *
 * @param chart the chart itself (excluding axes, markers, etc.). You can use [lineChart] or [columnChart], or provide a
 * custom [Chart] implementation.
 * @param chartModelProducer creates and updates the [ChartEntryModel] for the chart.
 * @param modifier the modifier to be applied to the chart.
 * @param startAxis the axis displayed at the start of the chart.
 * @param topAxis the axis displayed at the top of the chart.
 * @param endAxis the axis displayed at the end of the chart.
 * @param bottomAxis the axis displayed at the bottom of the chart.
 * @param marker appears when the chart is touched, highlighting the entry or entries nearest to the touch point.
 * @param markerVisibilityChangeListener allows for listening to [marker] visibility changes.
 * @param legend an optional legend for the chart.
 * @param chartScrollSpec houses scrolling-related settings.
 * @param isZoomEnabled whether zooming in and out is enabled.
 * @param diffAnimationSpec the animation spec used for difference animations.
 * @param runInitialAnimation whether to display an animation when the chart is created. In this animation, the value
 * of each chart entry is animated from zero to the actual value.
 * @param fadingEdges applies a horizontal fade to the edges of the chart area for scrollable charts.
 * @param autoScaleUp defines whether the content of a scrollable chart should be scaled up when the entry count and
 * intrinsic segment width are such that, at a scale factor of 1, an empty space would be visible near the end edge of
 * the chart.
 * @param chartScrollState houses information on the chart’s scroll state. Allows for programmatic scrolling.
 */
@Composable
public fun <Model : ChartEntryModel> Chart(
    chart: Chart<Model>,
    chartModelProducer: ChartModelProducer<Model>,
    modifier: Modifier = Modifier,
    startAxis: VerticalAxis<AxisPosition.Vertical.Start>? = null,
    topAxis: HorizontalAxis<AxisPosition.Horizontal.Top>? = null,
    endAxis: VerticalAxis<AxisPosition.Vertical.End>? = null,
    bottomAxis: HorizontalAxis<AxisPosition.Horizontal.Bottom>? = null,
    marker: Marker? = null,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener? = null,
    legend: Legend? = null,
    chartScrollSpec: ChartScrollSpec<Model> = rememberChartScrollSpec(),
    isZoomEnabled: Boolean = true,
    diffAnimationSpec: AnimationSpec<Float> = defaultDiffAnimationSpec,
    runInitialAnimation: Boolean = true,
    fadingEdges: FadingEdges? = null,
    autoScaleUp: AutoScaleUp = AutoScaleUp.Full,
    chartScrollState: ChartScrollState = rememberChartScrollState(),
) {
    val modelState: MutableSharedState<Model?, Model?> = chartModelProducer.collectAsState(
        chartKey = chart,
        producerKey = chartModelProducer,
        animationSpec = diffAnimationSpec,
        runInitialAnimation = runInitialAnimation,
    )

    ChartBox(modifier = modifier) {
        modelState.value?.also { model ->
            ChartImpl(
                chart = chart,
                model = model,
                oldModel = modelState.previousValue,
                startAxis = startAxis,
                topAxis = topAxis,
                endAxis = endAxis,
                bottomAxis = bottomAxis,
                marker = marker,
                markerVisibilityChangeListener = markerVisibilityChangeListener,
                legend = legend,
                chartScrollSpec = chartScrollSpec,
                isZoomEnabled = isZoomEnabled,
                fadingEdges = fadingEdges,
                autoScaleUp = autoScaleUp,
                chartScrollState = chartScrollState,
            )
        }
    }
}

/**
 * Displays a chart.
 *
 * This function accepts a [ChartEntryModel]. For dynamic data, use the function overload that accepts a
 * [ChartModelProducer] instance.
 *
 * @param chart the chart itself (excluding axes, markers, etc.). You can use [lineChart] or [columnChart], or provide a
 * custom [Chart] implementation.
 * @param model the [ChartEntryModel] for the chart.
 * @param modifier the modifier to be applied to the chart.
 * @param startAxis the axis displayed at the start of the chart.
 * @param topAxis the axis displayed at the top of the chart.
 * @param endAxis the axis displayed at the end of the chart.
 * @param bottomAxis the axis displayed at the bottom of the chart.
 * @param marker appears when the chart is touched, highlighting the entry or entries nearest to the touch point.
 * @param markerVisibilityChangeListener allows for listening to [marker] visibility changes.
 * @param legend an optional legend for the chart.
 * @param chartScrollSpec houses scrolling-related settings.
 * @param isZoomEnabled whether zooming in and out is enabled.
 * @param oldModel the chart’s previous [ChartEntryModel]. This is used to determine whether to perform an automatic
 * scroll.
 * @param fadingEdges applies a horizontal fade to the edges of the chart area for scrollable charts.
 * @param autoScaleUp defines whether the content of a scrollable chart should be scaled up when the entry count and
 * intrinsic segment width are such that, at a scale factor of 1, an empty space would be visible near the end edge of
 * the chart.
 * @param chartScrollState houses information on the chart’s scroll state. Allows for programmatic scrolling.
 */
@Composable
public fun <Model : ChartEntryModel> Chart(
    chart: Chart<Model>,
    model: Model,
    modifier: Modifier = Modifier,
    startAxis: VerticalAxis<AxisPosition.Vertical.Start>? = null,
    topAxis: HorizontalAxis<AxisPosition.Horizontal.Top>? = null,
    endAxis: VerticalAxis<AxisPosition.Vertical.End>? = null,
    bottomAxis: HorizontalAxis<AxisPosition.Horizontal.Bottom>? = null,
    marker: Marker? = null,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener? = null,
    legend: Legend? = null,
    chartScrollSpec: ChartScrollSpec<Model> = rememberChartScrollSpec(),
    isZoomEnabled: Boolean = true,
    oldModel: Model? = null,
    fadingEdges: FadingEdges? = null,
    autoScaleUp: AutoScaleUp = AutoScaleUp.Full,
    chartScrollState: ChartScrollState = rememberChartScrollState(),
) {
    ChartBox(modifier = modifier) {
        ChartImpl(
            chart = chart,
            model = model,
            startAxis = startAxis,
            topAxis = topAxis,
            endAxis = endAxis,
            bottomAxis = bottomAxis,
            marker = marker,
            markerVisibilityChangeListener = markerVisibilityChangeListener,
            legend = legend,
            chartScrollSpec = chartScrollSpec,
            isZoomEnabled = isZoomEnabled,
            oldModel = oldModel,
            fadingEdges = fadingEdges,
            autoScaleUp = autoScaleUp,
            chartScrollState = chartScrollState,
        )
    }
}
@Composable
public fun <Model : ChartEntryModel> ChartImpl(
    chart: Chart<Model>,
    startAxis: VerticalAxis<AxisPosition.Vertical.Start>? = null,
    topAxis: HorizontalAxis<AxisPosition.Horizontal.Top>? = null,
    endAxis: VerticalAxis<AxisPosition.Vertical.End>? = null,
    bottomAxis: HorizontalAxis<AxisPosition.Horizontal.Bottom>? = null,
    model: Model,
    modifier: Modifier = Modifier,
    marker: Marker?,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener?,
    legend: Legend?,
    chartScrollSpec: ChartScrollSpec<Model>,
    isZoomEnabled: Boolean,
    oldModel: Model? = null,
    fadingEdges: FadingEdges?,
    autoScaleUp: AutoScaleUp,
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

    LaunchedEffect(key1 = model.id) {
        chartScrollSpec.performAutoScroll(
            model = model,
            oldModel = oldModel,
            chartScrollState = chartScrollState,
        )
    }

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

        val chartBounds = virtualLayout.setBounds(
            subcomposeMeasureScope = this,
            density = this,
            layoutDirection = this.layoutDirection,
            context = measureContext,
            contentBounds = bounds,
            chart = chart,
            legend = legend,
            segmentProperties = segmentProperties,
            null,
        )

        if (chartBounds.isEmpty) {
            return@SubcomposeLayout layout(constraints.maxWidth, constraints.maxHeight) { }
        }

        chartScrollState.maxValue = measureContext.getMaxScrollDistance(
            layoutDirection = this.layoutDirection,
            chartWidth = chart.bounds.width,
            segmentProperties = segmentProperties,
        )

        chartScrollState.handleInitialScroll(initialScroll = chartScrollSpec.initialScroll)

        val startAxisPlaceables = axisManager.startAxis?.getPlaceables(this, measureContext, chart.bounds.roundToIntRect().size)

        val endAxisPlaceables = axisManager.endAxis?.getPlaceables(this, measureContext, chart.bounds.roundToIntRect().size)

        val chartPlaceable = subcompose("chart", { Box(Modifier.fillMaxSize()) }).map {
            it.measure(constraints.copy(maxWidth = chart.bounds.width.toInt(), maxHeight = chart.bounds.height.toInt(), minWidth = 0, minHeight = 0))
        }.first()

        layout(constraints.maxWidth, constraints.maxHeight) {
            startAxisPlaceables?.run {
                startAxis?.run {
                    placeAxis(
                        layoutDirection = this@SubcomposeLayout.layoutDirection,
                        axisLine = startAxisPlaceables.axis,
                        axisOffset = axisManager.startAxis.bounds.width.toInt(),
                        axisLabelPlaceables = startAxisPlaceables.labels,
                        tickPlaceables = startAxisPlaceables.ticks,
                        guidelinePlaceables = startAxisPlaceables.guidelines,
                        constraints = constraints,
                        chartBounds = chartBounds,
                    )
                }
            }
            endAxisPlaceables?.run {
                endAxis?.run {
                    placeAxis(
                        layoutDirection = this@SubcomposeLayout.layoutDirection,
                        axisLine = endAxisPlaceables.axis,
                        axisOffset =  axisManager.endAxis.bounds.width.toInt(),
                        axisLabelPlaceables = endAxisPlaceables.labels,
                        tickPlaceables = endAxisPlaceables.ticks,
                        guidelinePlaceables = endAxisPlaceables.guidelines,
                        constraints = constraints,
                        chartBounds = chartBounds,
                    )
                }
            }

            chartPlaceable.place(chart.bounds.topLeft.round())
        }
    }
}
