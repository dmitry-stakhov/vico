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

package com.patrykandpatrick.vico.core.component.dimension

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions

/**
 * Sets a padding value for each edge of the rectangle individually.
 */
@Composable
public fun <P : Padding> P.setPadding(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
): P = apply {
    padding.set(
        start = start,
        top = top,
        end = end,
        bottom = bottom,
    )
}

/**
 * Sets the horizontal and vertical padding for the rectangle.
 */
@Composable
public fun <P : Padding> P.setPadding(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
): P = apply {
    padding.set(
        start = horizontal,
        top = vertical,
        end = horizontal,
        bottom = vertical,
    )
}

/**
 * Sets a common padding value for each edge of the rectangle.
 */
@Composable
public fun <P : Padding> P.setPadding(
    all: Dp = 0.dp,
): P = apply {
    padding.set(
        start = all,
        top = all,
        end = all,
        bottom = all,
    )
}

/**
 * Allows a component to implement padding.
 */
public interface Padding {

    /**
     * The current padding.
     */
    public val padding: MutableDimensions

    /**
     * Updates the padding for each side individually.
     */
    public fun setPadding(
        start: Dp = 0.dp,
        top: Dp = 0.dp,
        end: Dp = 0.dp,
        bottom: Dp = 0.dp,
    ) {
        padding.set(start, top, end, bottom)
    }

    /**
     * Updates the horizontal and vertical padding.
     */
    public fun setPadding(
        horizontal: Dp = 0.dp,
        vertical: Dp = 0.dp,
    ) {
        padding.set(horizontal, vertical, horizontal, vertical)
    }

    /**
     * Sets a common padding value for each side.
     */
    public fun setPadding(
        all: Dp = 0.dp,
    ) {
        padding.set(all)
    }
}
