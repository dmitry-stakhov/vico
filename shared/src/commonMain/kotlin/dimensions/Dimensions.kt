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

package com.patrykandpatrick.vico.core.dimensions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Defines the size of each edge of a rectangle.
 * Used to store measurements such as padding or margin values.
 */
public interface Dimensions {

    /**
     * The value for the start edge in the dp unit.
     */
    public val start: Dp

    /**
     * The value for the top edge in the dp unit.
     */
    public val top: Dp

    /**
     * The value for the end edge in the dp unit.
     */
    public val end: Dp

    /**
     * The value for the bottom edge in the dp unit.
     */
    public val bottom: Dp

    /**
     * Returns the dimension of the left edge depending on the layout orientation.
     *
     * @param isLtr whether the device layout is left-to-right.
     */
    public fun getLeftDp(isLtr: Boolean): Dp = if (isLtr) start else end

    /**
     * Returns the dimension of the right edge depending on the layout orientation.
     *
     * @param isLtr whether the device layout is left-to-right.
     */
    public fun getRightDp(isLtr: Boolean): Dp = if (isLtr) end else start
}

/**
 * Creates a [MutableDimensions] instance with a common value for each coordinate.
 */
public fun dimensionsOf(all: Dp): MutableDimensions = dimensionsOf(
    start = all,
    top = all,
    end = all,
    bottom = all,
)

/**
 * Creates a [MutableDimensions] instance using the provided measurements.
 */
public fun dimensionsOf(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
): MutableDimensions = MutableDimensions(
    start = horizontal,
    top = vertical,
    end = horizontal,
    bottom = vertical,
)

/**
 * Creates a [MutableDimensions] instance using the provided measurements.
 */
public fun dimensionsOf(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
): MutableDimensions = MutableDimensions(
    start = start,
    top = top,
    end = end,
    bottom = bottom,
)
