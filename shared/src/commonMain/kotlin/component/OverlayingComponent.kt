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

package com.patrykandpatrick.vico.core.component

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.debug.DebugHelper

/**
 * A [Component] composed out of two [Component]s, with one drawn over the other.
 * @property outer the outer (background) [Component].
 * @property inner the inner (foreground) [Component].
 * @property innerPaddingStart the start padding between the inner and outer components.
 * @property innerPaddingTop the top padding between the inner and outer components.
 * @property innerPaddingEnd the end padding between the inner and outer components.
 * @property innerPaddingBottom the bottom padding between the inner and outer components.
 */
public class OverlayingComponent(
    public val outer: Component,
    public val inner: Component,
    public val innerPaddingStart: Dp = 0.dp,
    public val innerPaddingTop: Dp = 0.dp,
    public val innerPaddingEnd: Dp = 0.dp,
    public val innerPaddingBottom: Dp = 0.dp,
) : Component() {

    public constructor(
        outer: Component,
        inner: Component,
        innerPaddingAll: Dp = 0.dp,
    ) : this(
        outer = outer,
        inner = inner,
        innerPaddingStart = innerPaddingAll,
        innerPaddingTop = innerPaddingAll,
        innerPaddingEnd = innerPaddingAll,
        innerPaddingBottom = innerPaddingAll,
    )

    init {
//        inner.margins = MutableDimensions(
//            start = innerPaddingStart,
//            top = innerPaddingTop,
//            end = innerPaddingEnd,
//            bottom = innerPaddingBottom,
//        )
    }

    override fun draw(
        drawScope: DrawScope,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Unit = with(drawScope) {
        val leftWithMargin = left + margins.start.toPx()
        val topWithMargin = top + margins.top.toPx()
        val rightWithMargin = right - margins.end.toPx()
        val bottomWithMargin = bottom - margins.bottom.toPx()

        outer.draw(this, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin)
        inner.draw(this, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin)

        DebugHelper.drawDebugBounds(
            context = this,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
        )
    }
}
