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

package com.patrykandpatrick.vico.core.component.shape

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.DEF_SHADOW_COLOR
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShader
import com.patrykandpatrick.vico.core.component.shape.shadow.ComponentShadow
import com.patrykandpatrick.vico.core.debug.DebugHelper
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions
import com.patrykandpatrick.vico.core.extension.alpha
import com.patrykandpatrick.vico.core.extension.half
import kotlin.properties.Delegates

/**
 * [ShapeComponent] is a [Component] that draws a shape.
 *
 * @param shape the [Shape] that will be drawn.
 * @param color the color of the shape.
 * @param dynamicShader an optional [Shader] provider used as the shape’s background.
 * @param margins the [Component]’s margins.
 * @param strokeWidth the width of the shape’s stroke (in dp).
 * @param strokeColor the color of the stroke.
 */
public open class ShapeComponent(
    public val shape: Shape = RectangleShape,
    color: Int = Color.Black.toArgb(),
    public val dynamicShader: DynamicShader? = null,
    public override val margins: MutableDimensions = emptyDimensions(),
    public val strokeWidth: Dp = 0.dp,
    strokeColor: Int = Color.Transparent.toArgb(),
) : Component() {

    private val paint: Paint = Paint().apply { isAntiAlias = true }
    private val strokePaint: Paint = Paint().apply { isAntiAlias = true }
    private val shadowProperties: ComponentShadow = ComponentShadow()

    protected val path: Path = Path()

    /**
     * The color of the shape.
     */
    public var color: Int by Delegates.observable(color) { _, _, value ->
        paint.color = Color(value)
    }

    /**
     * The color of the stroke.
     */
    public var strokeColor: Int by Delegates.observable(strokeColor) { _, _, value ->
        strokePaint.color = Color(value)
    }

    init {
        paint.color = Color(color)

        with(strokePaint) {
            this.color = Color(strokeColor)
            style = PaintingStyle.Stroke
        }
    }

    override fun draw(
        drawScope: DrawScope,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Unit = with(drawScope) {
        if (left == right || top == bottom) return // Skip drawing shape that will be invisible.
        path.reset() // rewind
        applyShader(left, top, right, bottom)
        val centerX = (left + right).half
        val centerY = (top + bottom).half
        shadowProperties.maybeUpdateShadowLayer(
            density = this,
            paint = paint,
            backgroundColor = color
        )

        val strokeWidthPx = strokeWidth.toPx()
        strokePaint.strokeWidth = strokeWidthPx

        fun drawShape(paint: Paint) {
            val outline = shape.createOutline(
                size = Size(
                    width = maxOf(right - margins.end.toPx() - strokeWidthPx.half, centerX) -
                            minOf(left + margins.start.toPx() + strokeWidthPx.half, centerX),
                    height = maxOf(bottom - margins.bottom.toPx() - strokeWidthPx.half, centerY) -
                            minOf(top + margins.top.toPx() + strokeWidthPx.half, centerY)
                ),
                LayoutDirection.Ltr,
                this
            )
            withTransform({ translate(left, top) }) {
                drawOutline(outline, paint.color)
            }
        }

        drawShape(paint)
        if (strokeWidthPx > 0f && strokeColor.alpha > 0) drawShape(strokePaint)

        DebugHelper.drawDebugBounds(
            context = this,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
        )
    }

    protected fun applyShader(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        dynamicShader
            ?.provideShader(left, top, right, bottom)
//            ?.let { shader -> paint.shader = shader }
    }

    /**
     * Applies a drop shadow.
     *
     * @param radius the blur radius.
     * @param dx the horizontal offset.
     * @param dy the vertical offset.
     * @param color the shadow color.
     * @param applyElevationOverlay whether to apply an elevation overlay to the shape.
     */
    public fun setShadow(
        radius: Float,
        dx: Float = 0f,
        dy: Float = 0f,
        color: Int = DEF_SHADOW_COLOR,
        applyElevationOverlay: Boolean = false,
    ): ShapeComponent = apply {
        shadowProperties.apply {
            this.radius = radius
            this.dx = dx
            this.dy = dy
            this.color = color
            this.applyElevationOverlay = applyElevationOverlay
        }
    }

    /**
     * Removes this [ShapeComponent]’s drop shadow.
     */
    public fun clearShadow(): ShapeComponent = apply {
        shadowProperties.apply {
            this.radius = 0f
            this.dx = 0f
            this.dy = 0f
            this.color = 0
        }
    }
}
