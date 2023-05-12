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

package com.patrykandpatrick.vico.core.component.shape.shader

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.unit.Dp
import com.patrykandpatrick.vico.compose.component.shape.shader.BrushShader
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.context.DrawContext
import org.jetbrains.skia.Bitmap

/**
 * Creates a [ComponentShader] out of the provided [component].
 *
 * @property component used as a pattern in the [Shader].
 * @property componentSize the size of the [component].
 * @property checkeredArrangement whether the [component] will be arranged in a checkered pattern.
 * @property tileXMode the horizontal tiling mode for the [component].
 * @property tileYMode the vertical tiling mode for the [component].
 */
public fun DynamicShaders.fromComponent(
    component: Component,
    componentSize: Dp,
    checkeredArrangement: Boolean = true,
    tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
    tileYMode: Shader.TileMode = tileXMode,
): ComponentShader = ComponentShader(
    component = component,
    componentSizeDp = componentSize.value,
    checkeredArrangement = checkeredArrangement,
    tileXMode = tileXMode,
    tileYMode = tileYMode,
)

/**
 * Creates a [BrushShader] using the given [Brush].
 *
 * @see BrushShader
 */
public fun DynamicShaders.fromBrush(brush: Brush): BrushShader = BrushShader(brush)

/**
 * An object that holds simple, anonymous implementations of [DynamicShader].
 */
public object DynamicShaders {

    /**
     * Creates a [DynamicShader] out of the given [bitmap].
     */
    public fun fromBitmap(
        bitmap: Bitmap,
        tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
        tileYMode: Shader.TileMode = tileXMode,
    ): DynamicShader = object : CacheableDynamicShader() {
        override fun createShader(
            context: DrawContext,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
        ): Shader = BitmapShader(bitmap, tileXMode, tileYMode)
    }
}
