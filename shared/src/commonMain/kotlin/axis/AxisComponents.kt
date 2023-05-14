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

package com.patrykandpatrick.vico.compose.axis

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.component.lineComponent
import com.patrykandpatrick.vico.compose.component.shape.shader.BrushShader
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.dimensions.Dimensions
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.dimensionsOf
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions

/**
 * Creates a label to be displayed on chart axes.
 *
 * @param color the text color.
 * @param textSize the text size.
 * @param background a [ShapeComponent] to be displayed behind the text.
 * @param ellipsize the text truncation behavior.
 * @param lineCount the line count.
 * @param verticalPadding the vertical padding between the text and the background.
 * @param horizontalPadding the horizontal padding between the text and the background.
 * @param verticalMargin the vertical margin around the background.
 * @param horizontalMargin the horizontal margin around the background.
 * @param typeface the typeface used for the label.
 * @param textAlign the text alignment.
 */
@Composable
public fun axisLabelComponent(
    color: Color = currentChartStyle.axis.axisLabelColor,
    textSize: TextUnit = currentChartStyle.axis.axisLabelTextSize,
    background: ShapeComponent? = currentChartStyle.axis.axisLabelBackground,
//    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    lineCount: Int = currentChartStyle.axis.axisLabelLineCount,
    verticalPadding: Dp = currentChartStyle.axis.axisLabelVerticalPadding,
    horizontalPadding: Dp = currentChartStyle.axis.axisLabelHorizontalPadding,
    verticalMargin: Dp = currentChartStyle.axis.axisLabelVerticalMargin,
    horizontalMargin: Dp = currentChartStyle.axis.axisLabelHorizontalMargin,
//    typeface: Typeface = currentChartStyle.axis.axisLabelTypeface,
//    textAlign: Paint.Align = currentChartStyle.axis.axisLabelTextAlign,
): TextComponent = textComponent(
    color = color,
    textSize = textSize,
    background = background,
//    ellipsize = ellipsize,
    lineCount = lineCount,
    padding = dimensionsOf(
        vertical = verticalPadding,
        horizontal = horizontalPadding,
    ),
    margins = dimensionsOf(
        vertical = verticalMargin,
        horizontal = horizontalMargin,
    ),
//    typeface = typeface,
//    textAlign = textAlign,
)

/**
 * Creates a [LineComponent] styled as an axis line.
 *
 * @param color the background color.
 * @param thickness the line thickness.
 * @param shape the [ChartShape] to use for the line.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the line.
 * @param margins the margins of the line.
 */
@Composable
public fun axisLineComponent(
    color: Color = currentChartStyle.axis.axisLineColor,
    thickness: Dp = currentChartStyle.axis.axisLineWidth,
    shape: Shape = currentChartStyle.axis.axisLineShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    dynamicShader: DynamicShader? = null,
    margins: MutableDimensions = emptyDimensions(),
): LineComponent = lineComponent(
    color = color,
    thickness = thickness,
    dynamicShader = dynamicShader,
    shape = shape,
    margins = margins,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
)

/**
 * Creates a [LineComponent] styled as an axis line.
 *
 * @param color the background color.
 * @param thickness the thickness of the line.
 * @param shape the [Shape] to use for the line.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param brush an optional [Brush] to apply to the line.
 * @param margins the margins of the line.
 */
@Composable
public fun axisLineComponent(
    color: Color,
    thickness: Dp = currentChartStyle.axis.axisLineWidth,
    shape: Shape = RectangleShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    brush: Brush? = null,
    margins: MutableDimensions = emptyDimensions(),
): LineComponent = lineComponent(
    color = color,
    thickness = thickness,
    dynamicShader = brush?.let(::BrushShader),
    shape = shape,
    margins = margins,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
)

/**
 * Creates a [LineComponent] styled as a tick line.
 *
 * @param color the background color.
 * @param thickness the thickness of the tick.
 * @param shape the [ChartShape] to use for the tick.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the tick.
 */
@Composable
public fun axisTickComponent(
    color: Color = currentChartStyle.axis.axisTickColor,
    thickness: Dp = currentChartStyle.axis.axisTickWidth,
    shape: Shape = currentChartStyle.axis.axisTickShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    dynamicShader: DynamicShader? = null,
): LineComponent = lineComponent(
    color = color,
    thickness = thickness,
    dynamicShader = dynamicShader,
    shape = shape,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
)

/**
 * Creates a [LineComponent] styled as a tick line.
 *
 * @param color the background color.
 * @param thickness the thickness of the line.
 * @param shape the [Shape] to use for the line.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param brush an optional [Brush] to apply to the line.
 */
@Composable
public fun axisTickComponent(
    color: Color,
    thickness: Dp = currentChartStyle.axis.axisTickWidth,
    shape: Shape = RectangleShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    brush: Brush? = null,
): LineComponent = lineComponent(
    color = color,
    thickness = thickness,
    dynamicShader = brush?.let(::BrushShader),
//    shape = shape.chartShape(),
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
)

/**
 * Creates an axis guideline.
 *
 * @param color the background color.
 * @param thickness the line thickness.
 * @param shape the [ChartShape] to use for the guideline.
 * @param strokeWidth the stroke width.
 * @param strokeColor the stroke color.
 * @param dynamicShader an optional [DynamicShader] to apply to the guideline.
 * @param margins the margins of the guideline.
 */
@Composable
public fun axisGuidelineComponent(
    color: Color = currentChartStyle.axis.axisGuidelineColor,
    thickness: Dp = currentChartStyle.axis.axisGuidelineWidth,
    shape: Shape = currentChartStyle.axis.axisGuidelineShape,
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
    dynamicShader: DynamicShader? = null,
    margins: MutableDimensions = emptyDimensions(),
): LineComponent = lineComponent(
    color = color,
    thickness = thickness,
    dynamicShader = dynamicShader,
    shape = shape,
    margins = margins,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
)
