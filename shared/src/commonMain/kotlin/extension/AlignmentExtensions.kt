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

package com.patrykandpatrick.vico.core.component.text

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import com.patrykandpatrick.vico.core.extension.half

public fun Alignment.Vertical.negative(): Alignment.Vertical = when (this) {
    Alignment.Top -> Alignment.Bottom
    Alignment.CenterVertically -> Alignment.CenterVertically
    Alignment.Bottom -> Alignment.Top
    else -> error("Not Supported")
}

internal fun Alignment.Vertical.inBounds(
    bounds: Rect,
    distanceFromPoint: Float = 0f,
    componentHeight: Float,
    y: Float,
): Alignment.Vertical {
    val topFits = y - distanceFromPoint - componentHeight >= bounds.top
    val centerFits = y - componentHeight.half >= bounds.top && y + componentHeight.half <= bounds.bottom
    val bottomFits = y + distanceFromPoint + componentHeight <= bounds.bottom

    return when (this) {
        Alignment.Top -> if (topFits) this else Alignment.Bottom
        Alignment.Bottom -> if (bottomFits) this else Alignment.Top
        Alignment.Center -> when {
            centerFits -> this
            topFits -> Alignment.Top
            else -> Alignment.Bottom
        }
        else -> error("Not supported")
    }
}
