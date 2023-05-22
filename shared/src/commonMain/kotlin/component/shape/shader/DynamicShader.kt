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

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shader

/**
 * [DynamicShader] creates [Shader] instances on demand.
 *
 * @see Shader
 */
public fun interface DynamicShader {

    /**
     * Creates a [Shader] by using the provided [bounds].
     */
    public fun provideShader(
        bounds: Rect,
    ): Shader = provideShader(
        left = bounds.left,
        top = bounds.top,
        right = bounds.right,
        bottom = bounds.bottom,
    )

    /**
     * Creates a [Shader] by using the provided [left], [top], [right], and [bottom] bounds.
     */
    public fun provideShader(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Shader
}
