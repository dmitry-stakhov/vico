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

package com.patrykandpatrick.vico.core.context

import androidx.compose.ui.geometry.Rect
import com.patrykandpatrick.vico.core.chart.Chart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager

/**
 * [MeasureContext] holds data used by various chart components during the measuring and drawing phases.
 */
public interface MeasureContext : Extras {

    /**
     * The bounds of the canvas that will be used to draw the chart and its components.
     */
    public val canvasBounds: Rect

    /**
     * Manages the associated [Chart]’s [ChartValues].
     *
     * @see [ChartValuesManager]
     */
    public val chartValuesManager: ChartValuesManager

    /**
     * The pixel density.
     */
    public val density: Float

    /**
     * The font scale.
     */
    public val fontScale: Float

    /**
     * Whether horizontal scrolling is enabled.
     */
    public val isHorizontalScrollEnabled: Boolean

    /**
     * The scale of the chart. Used to handle zooming in and out.
     */
    public val chartScale: Float

    /**
     * Removes all stored extras and resets [ChartValuesManager.chartValues].
     *
     * @see Extras.clearExtras
     * @see ChartValuesManager.resetChartValues
     */
    public fun reset()
}
