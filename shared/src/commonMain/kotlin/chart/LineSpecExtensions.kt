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

package com.patrykandpatrick.vico.core.chart

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.formatter.ValueFormatter

/**
 * Creates a new [LineChart.LineSpec] based on this one, updating select properties.
 */
public fun LineChart.LineSpec.copy(
    lineColor: Int = this.lineColor,
    lineThickness: Dp = this.lineThickness,
    lineBackgroundShader: DynamicShader? = this.lineBackgroundShader,
    lineCap: StrokeCap = this.lineCap,
    point: Component? = this.point,
    pointSize: Dp = this.pointSize,
    dataLabel: TextComponent? = this.dataLabel,
    dataLabelVerticalPosition: Alignment.Vertical = this.dataLabelVerticalPosition,
    dataLabelValueFormatter: ValueFormatter = this.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = this.dataLabelRotationDegrees,
    pointConnector: LineChart.LineSpec.PointConnector = this.pointConnector,
): LineChart.LineSpec = LineChart.LineSpec(
    lineColor = lineColor,
    lineThickness = lineThickness,
    lineBackgroundShader = lineBackgroundShader,
    lineCap = lineCap,
    point = point,
    pointSize = pointSize,
    dataLabel = dataLabel,
    dataLabelVerticalPosition = dataLabelVerticalPosition,
    dataLabelValueFormatter = dataLabelValueFormatter,
    dataLabelRotationDegrees = dataLabelRotationDegrees,
    pointConnector = pointConnector,
)
