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
 * An implementation of [Dimensions] whose every property is mutable.
 */
public class MutableDimensions(
    override var start: Dp,
    override var top: Dp,
    override var end: Dp,
    override var bottom: Dp,
) : Dimensions {

    public constructor(
        horizontal: Dp,
        vertical: Dp,
    ) : this(
        start = horizontal,
        top = vertical,
        end = horizontal,
        bottom = vertical,
    )

    /**
     * The sum of [start] and [end].
     */
    public val horizontal: Dp
        get() = start + end

    /**
     * The sum of [top] and [bottom].
     */
    public val vertical: Dp
        get() = top + bottom

    /**
     * Updates these [MutableDimensions] to match the provided [Dimensions].
     */
    public fun set(other: Dimensions): MutableDimensions =
        set(other.start, other.top, other.end, other.bottom)

    /**
     * Sets a common value for each coordinate.
     */
    public fun set(all: Dp): MutableDimensions =
        set(all, all, all, all)

    /**
     * Updates the coordinates to the provided values.
     */
    public fun set(
        start: Dp = 0.dp,
        top: Dp = 0.dp,
        end: Dp = 0.dp,
        bottom: Dp = 0.dp,
    ): MutableDimensions = apply {
        this.start = start
        this.top = top
        this.end = end
        this.bottom = bottom
    }

    /**
     * Evenly distributes the provided measurement between [start] and [end].
     */
    public fun setHorizontal(value: Dp): MutableDimensions = apply {
        start = if (value == 0.dp) value else value / 2
        end = if (value == 0.dp) value else value / 2
    }

    /**
     * Evenly distributes the provided measurement between [top] and [bottom].
     */
    public fun setVertical(value: Dp): MutableDimensions = apply {
        top = if (value == 0.dp) value else value / 2
        bottom = if (value == 0.dp) value else value / 2
    }

    /**
     * Sets all coordinates to 0.
     */
    public fun clear() {
        set(0.dp)
    }
}

/**
 * Creates a [MutableDimensions] instance with all coordinates set to 0.
 */
public fun emptyDimensions(): MutableDimensions = MutableDimensions(0.dp, 0.dp, 0.dp, 0.dp)
