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

package com.patrykandpatrick.vico.core.debug

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object DebugHelper {
    public var enabled: Boolean = false

    public var strokeWidth: Dp = 1.dp
    public var debugPaint = Paint().apply {
        style = PaintingStyle.Stroke
        color = Color.Magenta
    }

    public fun drawDebugBounds(
        context: DrawScope,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Unit = with(context) {
        if (!enabled) return@with
        debugPaint.strokeWidth = with(context) { strokeWidth.toPx() }
        context.drawContext.canvas.drawRect(left, top, right, bottom, debugPaint)
    }
}
