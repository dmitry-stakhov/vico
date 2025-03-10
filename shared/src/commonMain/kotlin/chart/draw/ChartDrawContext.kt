/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.model.Point
import extension.isLtr
import extension.layoutDirectionMultiplier

/**
 * An extension of [DrawContext] that holds additional data required to render a [Chart].
 */
public interface ChartDrawContext : DrawContext {

    /**
     * The bounds in which the [Chart] will be drawn.
     */
    public val chartBounds: Rect

    /**
     * Holds information about the width of each individual chart segment.
     */
    public val segmentProperties: SegmentProperties

    /**
     * The point inside the chart’s coordinates where physical touch is occurring.
     */
    public val markerTouchPoint: Point?

    /**
     * The current amount of horizontal scroll.
     */
    public val horizontalScroll: Float
}

/**
 * Returns the maximum scroll distance.
 */
public fun MeasureContext.getMaxScrollDistance(
    drawScope: DrawScope,
    chartWidth: Float,
    segmentProperties: SegmentProperties,
): Float {
    val cumulatedSegmentWidth = segmentProperties.segmentWidth *
            chartValuesManager.getChartValues().getDrawnEntryCount() *
            chartScale

    return (drawScope.layoutDirectionMultiplier * (cumulatedSegmentWidth - chartWidth)).run {
        if (drawScope.isLtr) coerceAtLeast(minimumValue = 0f) else coerceAtMost(maximumValue = 0f)
    }
}

/**
 * Returns the maximum scroll distance.
 */
public fun ChartDrawContext.getMaxScrollDistance(drawScope: DrawScope): Float =
    getMaxScrollDistance(
        drawScope = drawScope,
        chartWidth = chartBounds.width,
        segmentProperties = segmentProperties,
    )
