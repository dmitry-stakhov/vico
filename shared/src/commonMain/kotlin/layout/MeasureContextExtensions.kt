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

package com.patrykandpatrick.vico.compose.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.context.MutableMeasureContext

/**
 * The anonymous implementation of the [MeasureContext].
 *
 * @param isHorizontalScrollEnabled whether horizontal scrolling is enabled.
 * @param chartScale the scale of the chart. Used to handle zooming in and out.
 * @param canvasBounds the bounds of the canvas that will be used to draw the chart and its components.
 */
@Composable
public fun getMeasureContext(
    isHorizontalScrollEnabled: Boolean,
    chartScale: Float,
    canvasBounds: Rect,
): MutableMeasureContext = remember {
    MutableMeasureContext(
        canvasBounds = canvasBounds,
        density = 0f,
        fontScale = 0f,
        isHorizontalScrollEnabled = isHorizontalScrollEnabled,
        chartScale = chartScale,
    )
}.apply {
    this.density = LocalDensity.current.density
    this.fontScale = LocalDensity.current.fontScale * LocalDensity.current.density
    this.isHorizontalScrollEnabled = isHorizontalScrollEnabled
    this.chartScale = chartScale
}
