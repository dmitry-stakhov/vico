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

package component.shape

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.core.DEF_MARKER_TICK_SIZE
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.Extras
import com.patrykandpatrick.vico.core.extension.doubled
import com.patrykandpatrick.vico.core.extension.half

/**
 * [MarkerCorneredShape] is an extension of [CorneredShape] that supports drawing a triangular tick at a given point.
 *
 * @param topLeft specifies a [Corner] for the top left of the [Shape].
 * @param topRight specifies a [Corner] for the top right of the [Shape].
 * @param bottomLeft specifies a [Corner] for the bottom left of the [Shape].
 * @param bottomRight specifies a [Corner] for the bottom right of the [Shape].
 * @param tickSizeDp the size of the tick (in dp).
 */
public open class MarkerCorneredShape(
    topLeft: Float,
    topRight: Float,
    bottomRight: Float,
    bottomLeft: Float,
    public val tickSizeDp: Float = DEF_MARKER_TICK_SIZE,
) : CornerBasedShape(CornerSize(topLeft), CornerSize(topRight), CornerSize(bottomRight), CornerSize(bottomLeft)) {

    public constructor(
        all: Float,
        tickSizeDp: Float = DEF_MARKER_TICK_SIZE,
    ) : this(all, all, all, all, tickSizeDp)

//    public constructor(
//        corneredShape: Float,
//        tickSizeDp: Float = DEF_MARKER_TICK_SIZE,
//    ) : this(
//        topLeft = corneredShape.,
//        topRight = corneredShape.topRight,
//        bottomRight = corneredShape.bottomRight,
//        bottomLeft = corneredShape.bottomLeft,
//        tickSizeDp = tickSizeDp,
//    )

//    override fun drawShape(
//        context: DrawContext,
//        paint: Paint,
//        path: Path,
//        left: Float,
//        top: Float,
//        right: Float,
//        bottom: Float,
//    ): Unit = with(context) {
//        val tickX: Float? = context[tickXKey]
//        if (tickX != null) {
//            createPath(
//                context = context,
//                path = path,
//                left = left,
//                top = top,
//                right = right,
//                bottom = bottom,
//            )
//            val tickSize = context.toPixels(tickSizeDp)
//            val availableCornerSize = minOf(right - left, bottom - top)
//            val cornerScale = getCornerScale(right - left, bottom - top, density)
//
//            val minLeft = left + bottomLeft.getCornerSize(availableCornerSize, density) * cornerScale
//            val maxLeft = right - bottomRight.getCornerSize(availableCornerSize, density) * cornerScale
//
//            val coercedTickSize = tickSize.coerceAtMost((maxLeft - minLeft).half.coerceAtLeast(0f))
//
//            (tickX - coercedTickSize)
//                .takeIf { minLeft < maxLeft }
//                ?.coerceIn(minLeft, maxLeft - coercedTickSize.doubled)
//                ?.also { tickTopLeft ->
//                    path.moveTo(tickTopLeft, bottom)
//                    path.lineTo(tickX, bottom + tickSize)
//                    path.lineTo(tickTopLeft + coercedTickSize.doubled, bottom)
//                }
//
//            path.close()
//            context.canvas.drawPath(path, paint)
//        } else {
//            super.drawShape(context, paint, path, left, top, right, bottom)
//        }
//    }

    public companion object {
        /**
         * Used to store and retrieve the _x_ coordinate of the tick.
         *
         * @see Extras
         */
        public const val tickXKey: String = "tickX"
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize
    ): CornerBasedShape {
        return RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)
    }

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection
    ): Outline {
        TODO("Not yet implemented")
    }
}
