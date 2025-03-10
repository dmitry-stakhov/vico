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

package com.patrykandpatrick.vico.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.component.shape.shader.toDynamicShader
import com.patrykandpatrick.vico.compose.extension.pixelSize
import com.patrykandpatrick.vico.core.DEF_LABEL_LINE_COUNT
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.OverlayingComponent
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions

/**
 * Creates a [LineComponent] with the specified properties.
 */
@Composable
public fun lineComponent(
    color: Color = Color.Black,
    thickness: Dp = DefaultDimens.COLUMN_WIDTH.dp,
    shape: Shape = RectangleShape,
    dynamicShader: DynamicShader? = null,
    margins: MutableDimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): LineComponent = LineComponent(
    color = color.toArgb(),
    thickness = thickness,
    shape = shape,
    dynamicShader = dynamicShader,
    margins = margins,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor.toArgb(),
)

/**
 * Creates a [ShapeComponent] with the specified properties.
 */
@Composable
public fun shapeComponent(
    shape: Shape = RectangleShape,
    color: Color = Color.Black,
    dynamicShader: DynamicShader? = null,
    margins: MutableDimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): ShapeComponent = ShapeComponent(
    shape = shape,
    color = color.toArgb(),
    dynamicShader = dynamicShader,
    margins = margins,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor.toArgb(),
)

/**
 * Creates a [ShapeComponent] with the specified properties.
 */
@Composable
public fun shapeComponent(
    shape: Shape = RectangleShape,
    color: Color = Color.Black,
    brush: Brush,
    margins: MutableDimensions = emptyDimensions(),
    strokeWidth: Dp = 0.dp,
    strokeColor: Color = Color.Transparent,
): ShapeComponent = shapeComponent(
    shape = shape,
    color = color,
    dynamicShader = brush.toDynamicShader(),
    margins = margins,
    strokeWidth = strokeWidth,
    strokeColor = strokeColor,
)

/**
 * Creates an [OverlayingComponent].
 *
 * @param outer the outer (background) [Component].
 * @param inner the inner (foreground) [Component].
 * @property innerPaddingStart the start padding between the inner and outer components.
 * @property innerPaddingTop the top padding between the inner and outer components.
 * @property innerPaddingEnd the end padding between the inner and outer components.
 * @property innerPaddingBottom the bottom padding between the inner and outer components.
 */
@Composable
public fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingStart: Dp = 0.dp,
    innerPaddingTop: Dp = 0.dp,
    innerPaddingBottom: Dp = 0.dp,
    innerPaddingEnd: Dp = 0.dp,
): OverlayingComponent = remember(
    outer,
    inner,
    innerPaddingStart,
    innerPaddingTop,
    innerPaddingBottom,
    innerPaddingEnd,
) {
    OverlayingComponent(
        outer = outer,
        inner = inner,
        innerPaddingStart = innerPaddingStart,
        innerPaddingTop = innerPaddingTop,
        innerPaddingBottom = innerPaddingBottom,
        innerPaddingEnd = innerPaddingEnd,
    )
}

/**
 * Creates an [OverlayingComponent].
 *
 * @param outer the outer (background) [Component].
 * @param inner the inner (foreground) [Component].
 * @param innerPaddingAll the padding between the inner and outer components.
 */
@Composable
public fun overlayingComponent(
    outer: Component,
    inner: Component,
    innerPaddingAll: Dp,
): OverlayingComponent = overlayingComponent(
    outer = outer,
    inner = inner,
    innerPaddingStart = innerPaddingAll,
    innerPaddingTop = innerPaddingAll,
    innerPaddingBottom = innerPaddingAll,
    innerPaddingEnd = innerPaddingAll,
)

/**
 * Creates a [TextComponent].
 *
 * @param color the text color.
 * @param textSize the text size.
 * @param background an optional [ShapeComponent] to display behind the text.
 * @param ellipsize the text truncation behavior.
 * @param lineCount the line count.
 * @param padding the padding between the text and the background.
 * @param margins the margins around the background.
 * @param typeface the [Typeface] for the text.
 * @param textAlign the text alignment.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
public fun textComponent(
    color: Color = Color.Black,
    textSize: TextUnit = DefaultDimens.TEXT_COMPONENT_TEXT_SIZE.sp,
    background: ShapeComponent? = null,
//    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    lineCount: Int = DEF_LABEL_LINE_COUNT,
    padding: MutableDimensions = emptyDimensions(),
    margins: MutableDimensions = emptyDimensions(),
    typeface: Typeface? = null,
//    textAlign: Paint.Align = Paint.Align.LEFT,
): TextComponent {
    val textMeasurer = rememberTextMeasurer()
    return remember(
        color,
        textSize,
        background,
//    ellipsize,
        lineCount,
        padding,
        margins,
        typeface,
//    textAlign,
        textMeasurer
    ) {
        textComponent {
            this.color = color.toArgb()
            textSizeSp = textSize.pixelSize()
//        this.ellipsize = ellipsize
            this.lineCount = lineCount
            this.background = background
            this.padding = padding
            this.margins = margins
            this.typeface = typeface
//        this.textAlign = textAlign
            this.textMeasurer = textMeasurer
        }
    }
}
