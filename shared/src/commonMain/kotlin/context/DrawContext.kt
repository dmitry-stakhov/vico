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

package com.patrykandpatrick.vico.core.context

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.extension.saveLayer

/**
 * [DrawContext] is an extension of [MeasureContext] that stores a [Canvas] and other properties.
 * It also defines helpful drawing functions.
 */
public interface DrawContext : MeasureContext {

    /**
     * The elevation overlay color, applied to [ShapeComponent]s that cast shadows.
     */
    public val elevationOverlayColor: Long

    public val drawScope: DrawScope

    /**
     * Saves the [Canvas] state.
     *
     * @see Canvas.save
     */
    public fun saveCanvas(): Unit = drawScope.drawContext.canvas.save()

    /**
     * Clips the [Canvas] to the specified rectangle.
     *
     * @see Canvas.clipRect
     */
    public fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
        drawScope.drawContext.canvas.clipRect(left, top, right, bottom)
    }

    /**
     * Clips the [Canvas] to the specified [rectF].
     *
     * @see Canvas.clipRect
     */
    public fun clipRect(rectF: Rect) {
        drawScope.drawContext.canvas.clipRect(rectF)
    }

    /**
     * Restores the [Canvas] state.
     *
     * @see Canvas.restore
     */
    public fun restoreCanvas() {
        drawScope.drawContext.canvas.restore()
    }

    /**
     * Restores the [Canvas] state to the given save level.
     *
     * @see Canvas.restoreToCount
     */
    public fun restoreCanvasToCount(count: Int) {
        // TODO
//        canvas.restoreToCount(count)
    }

    /**
     * A convenience function for [Canvas.saveLayer].
     *
     * @see Canvas.saveLayer
     */
    public fun saveLayer(
        left: Float = 0f,
        top: Float = 0f,
        right: Float = Float.MAX_VALUE,
        bottom: Float = Float.MAX_VALUE,
    ): Int = drawScope.drawContext.canvas.saveLayer(left, top, right, bottom)
}
