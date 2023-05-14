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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.extension.ceil
import kotlin.math.roundToInt

/**
 * [DashedShape] draws a dashed line by interchangeably drawing the provided [shape] and leaving a gap.
 *
 * @property shape the base [Shape] from which to create the [DashedShape].
 * @property dashLength the dash length in dp.
 * @property gapLength the gap length in dp.
 * @property fitStrategy the [DashedShape.FitStrategy] to use for the dashes.
 */
public class DashedShape(
    public val shape: Shape = RectangleShape,
    public val dashLength: Dp = DefaultDimens.DASH_LENGTH.dp,
    public val gapLength: Dp = DefaultDimens.DASH_GAP.dp,
    public val fitStrategy: FitStrategy = FitStrategy.Resize,
) : Shape {

    private var drawDashLength = dashLength.value
    private var drawGapLength = gapLength.value

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (size.width > size.height) {
            calculateDrawLengths(density, size.width)
        } else {
            calculateDrawLengths(density, size.height)
        }
        return Outline.Generic(Path().apply {
            val dashLengthPx = with(density) { dashLength.toPx() }
            val gapLengthPx = with(density) { gapLength.toPx() }
            val stepsCount = ((size.width + gapLengthPx) / (dashLengthPx + gapLengthPx)).roundToInt()
            val actualStep = size.width / stepsCount
            val dotSize = if (size.width > size.height) {
                Size(width = dashLengthPx, height = size.height)
            } else {
                Size(width = size.width, height = dashLengthPx)
            }
            for (i in 0 until stepsCount) {
                if (size.width > size.height) {
                    addRect(
                        Rect(
                            offset = Offset(x = i * actualStep, y = 0f),
                            size = dotSize
                        )
                    )
                } else {
                    addRect(
                        Rect(
                            offset = Offset(x = 0f, y = i * actualStep),
                            size = dotSize
                        )
                    )
                }
            }
            close()
        })
    }

    private fun calculateDrawLengths(density: Density, length: Float): Unit = with(density) {
        calculateDrawLengths(dashLength.toPx(), gapLength.toPx(), length)
    }

    private fun calculateDrawLengths(
        dashLength: Float,
        gapLength: Float,
        length: Float,
    ) {
        if (dashLength == 0f && gapLength == 0f) {
            drawDashLength = length
            return
        }
        when (fitStrategy) {
            FitStrategy.Resize -> when {
                length < dashLength + gapLength -> {
                    drawDashLength = length
                    drawGapLength = 0f
                }
                else -> {
                    val gapAndDashLength = gapLength + dashLength
                    val ratio = length / (dashLength + (length / gapAndDashLength).ceil * gapAndDashLength)
                    drawDashLength = dashLength * ratio
                    drawGapLength = gapLength * ratio
                }
            }
            FitStrategy.Fixed -> {
                drawDashLength = dashLength
                drawGapLength = gapLength
            }
        }
    }

    /**
     * Defines how a [DashedShape] is to be rendered.
     */
    public enum class FitStrategy {
        /**
         * The [DashedShape] will slightly increase or decrease the [DashedShape.dashLength] and
         * [DashedShape.gapLength] values so that the dashes fit perfectly without being cut off.
         */
        Resize,

        /**
         * The [DashedShape] will use the exact [DashedShape.dashLength] and [DashedShape.gapLength] values
         * provided. As a result, the [DashedShape] may not fit within its bounds or be cut off.
         */
        Fixed,
    }
}
