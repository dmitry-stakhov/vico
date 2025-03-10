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

package com.patrykandpatrick.vico.core.draw

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.patrykandpatrick.vico.core.DefaultColors
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.context.DefaultExtras
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.Extras

/**
 * Creates an anonymous implementation of [DrawContext].
 *
 * @param canvas the canvas to draw the chart on.
 * @param density the pixel density of the screen (used in pixel size calculation).
 * @param fontScale the scale of fonts.
 * @param isLtr whether the device layout is left-to-right.
 * @param elevationOverlayColor the elevation overlay color. This is applied to components that cast shadows.
 */
public fun drawContext(
    drawScope: DrawScope,
    density: Float = 1f,
    fontScale: Float = 1f,
    elevationOverlayColor: Long = DefaultColors.Light.elevationOverlayColor,
): DrawContext = object : DrawContext, Extras by DefaultExtras() {
    override val canvasBounds: Rect = Rect(0f, 0f, Float.MAX_VALUE, Float.MAX_VALUE)
    override val elevationOverlayColor: Long = elevationOverlayColor
    override val drawScope: DrawScope = drawScope
    override val density: Float = density
    override val fontScale: Float = fontScale
    override val isHorizontalScrollEnabled: Boolean = false
    override val chartScale: Float = 1f
    override val chartValuesManager: ChartValuesManager = ChartValuesManager()

    override fun reset() {
        chartValuesManager.resetChartValues()
        clearExtras()
    }
}
