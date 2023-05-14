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

package com.patrykandpatrick.vico.core.chart.edges

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.FADING_EDGE_VISIBILITY_THRESHOLD_DP
import com.patrykandpatrick.vico.core.FADING_EDGE_WIDTH_DP
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.getMaxScrollDistance
import com.patrykandpatrick.vico.core.extension.copyColor

private const val FULL_ALPHA = 0xFF
private const val FULL_FADE: Int = 0xFF000000.toInt()
private const val NO_FADE: Int = 0x00000000

/**
 * [FadingEdges] applies a horizontal fade to the edges of the chart area for scrollable charts.
 * This effect indicates that there’s more content beyond a given edge, and the user can scroll to reveal it.
 *
 * @param startEdgeWidthDp the width of the fade overlay for the start edge (in dp).
 * @param endEdgeWidthDp the width of the fade overlay for the end edge (in dp).
 * @param visibilityThresholdDp the scroll distance over which the overlays fade in and out (in dp).
 * @param visibilityInterpolator used for the fading edges’ fade-in and fade-out animations. This is a mapping of the
 * degree to which [visibilityThresholdDp] has been satisfied to the opacity of the fading edges.
 */
public open class FadingEdges(
    public var startEdgeWidthDp: Float = FADING_EDGE_WIDTH_DP,
    public var endEdgeWidthDp: Float = startEdgeWidthDp,
    public var visibilityThresholdDp: Float = FADING_EDGE_VISIBILITY_THRESHOLD_DP,
//    public var visibilityInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
) {

    private val paint: Paint = Paint()

    private var rect: Rect = Rect(0f, 0f, 0f, 0f)

    /**
     * Creates a [FadingEdges] instance with fading edges of equal width.
     *
     * @param edgeWidthDp the width of the fade overlay (in dp).
     * @param visibilityThresholdDp the scroll distance over which the overlays fade in and out (in dp).
     * @param visibilityInterpolator used for the fading edges’ fade-in and fade-out animations. This is a mapping of
     * the degree to which [visibilityThresholdDp] has been satisfied to the opacity of the fading edges.
     */
    public constructor(
        edgeWidthDp: Float = FADING_EDGE_WIDTH_DP,
        visibilityThresholdDp: Float = FADING_EDGE_VISIBILITY_THRESHOLD_DP,
//        visibilityInterpolator: TimeInterpolator = AccelerateDecelerateInterpolator(),
    ) : this(
        startEdgeWidthDp = edgeWidthDp,
        endEdgeWidthDp = edgeWidthDp,
        visibilityThresholdDp = visibilityThresholdDp,
//        visibilityInterpolator = visibilityInterpolator,
    )

    init {
        require(value = startEdgeWidthDp >= 0) { "`startEdgeWidthDp` must be greater than 0." }
        require(value = endEdgeWidthDp >= 0) { "`endEdgeWidthDp` must be greater than 0." }

        // TODO Check
//        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    /**
     * Applies fading edges inside of the given [bounds] accordingly to the scroll state.
     *
     * @param context the drawing context that holds the information necessary to draw the fading edges.
     * @param bounds the bounds within which the fading edges will be drawn.
     */
    public fun applyFadingEdges(
        drawScope: DrawScope,
        context: ChartDrawContext,
        bounds: Rect,
    ): Unit = with(context) {
        with(context.drawScope) {
            val maxScroll = getMaxScrollDistance(drawScope)
            var fadeAlphaFraction: Float

            if (isHorizontalScrollEnabled && startEdgeWidthDp > 0f && horizontalScroll > 0f) {
                fadeAlphaFraction =
                    (horizontalScroll / visibilityThresholdDp.dp.toPx()).coerceAtMost(1f)

                drawFadingEdge(
                    left = bounds.left,
                    top = bounds.top,
                    right = bounds.left + startEdgeWidthDp.dp.toPx(),
                    bottom = bounds.bottom,
                    direction = -1,
                    alpha = (FULL_ALPHA).toInt(),
//                alpha = (visibilityInterpolator.getInterpolation(fadeAlphaFraction) * FULL_ALPHA).toInt(),
                )
            }

            if (isHorizontalScrollEnabled && endEdgeWidthDp > 0f && horizontalScroll < maxScroll) {
                fadeAlphaFraction =
                    ((maxScroll - horizontalScroll) / visibilityThresholdDp.dp.toPx()).coerceAtMost(1f)

                drawFadingEdge(
                    left = bounds.right - endEdgeWidthDp.dp.toPx(),
                    top = bounds.top,
                    right = bounds.right,
                    bottom = bounds.bottom,
                    direction = 1,
                    alpha = (FULL_ALPHA).toInt(),
//                alpha = (visibilityInterpolator.getInterpolation(fadeAlphaFraction) * FULL_ALPHA).toInt(),
                )
            }
        }
    }

    private fun ChartDrawContext.drawFadingEdge(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        direction: Int,
        alpha: Int,
    ) {
        rect = Rect(left, top, right, bottom)

        val faded = Color(FULL_FADE.copyColor(alpha = alpha))

        paint.shader = LinearGradientShader(
            from = Offset(rect.left, 0f),
            to = Offset(rect.right, 0f),
            colors = if (direction < 0) listOf(faded, Color(NO_FADE)) else listOf(Color(NO_FADE), faded),
        )
        drawScope.drawContext.canvas.drawRect(rect, paint)
    }
}
