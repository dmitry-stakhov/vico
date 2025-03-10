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
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager

/**
 * A [MeasureContext] implementation that facilitates the mutation of some of its properties.
 */
public data class MutableMeasureContext(
    override val canvasBounds: Rect,
    override var density: Float,
    override var fontScale: Float,
    override var isHorizontalScrollEnabled: Boolean = false,
    override var chartScale: Float = 1f,
) : MeasureContext, Extras by DefaultExtras() {

    override val chartValuesManager: ChartValuesManager = ChartValuesManager()

    override fun reset() {
        clearExtras()
        chartValuesManager.resetChartValues()
    }
}
