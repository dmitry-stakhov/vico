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

package com.patrykandpatrick.vico.core.chart.draw

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.scale.AutoScaleUp
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.getClosestMarkerEntryModel
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerVisibilityChangeListener
import com.patrykandpatrick.vico.core.model.Point

/**
 * The anonymous implementation of [ChartDrawContext].
 *
 * @param canvas the canvas on which the [Chart] is to be drawn.
 * @param elevationOverlayColor the color of elevation overlays, applied to [ShapeComponent]s that cast shadows.
 * @param measureContext holds data used for component measurements.
 * @param markerTouchPoint the point inside the chart’s bounds where physical touch is occurring.
 * @param segmentProperties holds information about the width of each individual chart segment.
 * @param chartBounds the bounds in which the [Chart] will be drawn.
 * @param horizontalScroll the horizontal scroll.
 * @param autoScaleUp defines whether the content of a scrollable chart should be scaled up when the entry count and
 * intrinsic segment width are such that, at a scale factor of 1, an empty space would be visible near the end edge of
 * the chart.
 *
 * @see [ShapeComponent.setShadow]
 */
public fun chartDrawContext(
    drawScope: DrawScope,
    elevationOverlayColor: Int,
    measureContext: MeasureContext,
    markerTouchPoint: Point?,
    segmentProperties: SegmentProperties,
    chartBounds: Rect,
    horizontalScroll: Float,
    autoScaleUp: AutoScaleUp,
): ChartDrawContext = object : ChartDrawContext, MeasureContext by measureContext {

    override val chartBounds: Rect = chartBounds

    override val elevationOverlayColor: Long = elevationOverlayColor.toLong()

    override val drawScope: DrawScope = drawScope

    override val markerTouchPoint: Point? = markerTouchPoint

    override val chartScale: Float = calculateDrawScale()

    override val segmentProperties: SegmentProperties = segmentProperties.scaled(chartScale)

    override val horizontalScroll: Float = horizontalScroll

    private fun calculateDrawScale(): Float {
        val drawnEntryWidth = segmentProperties.segmentWidth * chartValuesManager.getChartValues().getDrawnEntryCount()
        val upscalingPossibleButDisallowed = drawnEntryWidth < chartBounds.width && autoScaleUp == AutoScaleUp.None
        val scrollEnabledAndUpscalingImpossible = isHorizontalScrollEnabled && drawnEntryWidth >= chartBounds.width
        return if (upscalingPossibleButDisallowed || scrollEnabledAndUpscalingImpossible) {
            measureContext.chartScale
        } else {
            chartBounds.width / drawnEntryWidth
        }
    }
}

internal inline fun ChartDrawContext.segmentWidth(density: Density): Int = with(density) {
    segmentProperties.segmentWidth.dp.toPx().toInt()
}

/**
 * Draws the provided [marker] on top of the chart at the given [markerTouchPoint] and notifies the
 * [markerVisibilityChangeListener] about the [marker]’s visibility changes.
 */
public fun <Model : ChartEntryModel> ChartDrawContext.drawMarker(
    drawScope: DrawScope,
    marker: Marker,
    markerTouchPoint: Point?,
    chart: Chart<Model>,
    markerVisibilityChangeListener: MarkerVisibilityChangeListener?,
    wasMarkerVisible: Boolean,
    setWasMarkerVisible: (Boolean) -> Unit,
    lastMarkerEntryModels: List<Marker.EntryModel>,
    onMarkerEntryModelsChange: (List<Marker.EntryModel>) -> Unit,
) {
    markerTouchPoint
        ?.let(chart.entryLocationMap::getClosestMarkerEntryModel)
        ?.let { markerEntryModels ->
            chartValuesManager.getChartValues()
            marker.draw(
                drawScope = drawScope,
                context = this,
                bounds = chart.bounds,
                markedEntries = markerEntryModels,
                chartValuesProvider = chartValuesManager,
            )
            if (wasMarkerVisible.not()) {
                markerVisibilityChangeListener?.onMarkerShown(
                    marker = marker,
                    markerEntryModels = markerEntryModels,
                )
                setWasMarkerVisible(true)
            }
            if (wasMarkerVisible && markerEntryModels != lastMarkerEntryModels) {
                onMarkerEntryModelsChange(markerEntryModels)
                markerVisibilityChangeListener?.onMarkerMoved(marker, markerEntryModels)
            }
        } ?: marker
        .takeIf { wasMarkerVisible }
        ?.also {
            markerVisibilityChangeListener?.onMarkerHidden(marker = marker)
            setWasMarkerVisible(false)
        }
}
