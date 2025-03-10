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

package com.patrykandpatrick.vico.core.model

import com.patrykandpatrick.vico.core.util.packFloats
import com.patrykandpatrick.vico.core.util.packInts
import com.patrykandpatrick.vico.core.util.unpackFloat1
import com.patrykandpatrick.vico.core.util.unpackFloat2
import kotlin.jvm.JvmInline

/**
 * Creates a new [Point] with the provided coordinates.
 */
public fun Point(x: Float, y: Float): Point = Point(packFloats(x, y))

/**
 * Creates a new [Point] with the provided coordinates.
 */
public fun Point(x: Int, y: Int): Point = Point(packInts(x, y))

/**
 * Represents a point in a coordinate system.
 */
@JvmInline
public value class Point internal constructor(private val packedValue: Long) {

    /**
     * The _x_ coordinate.
     */
    public val x: Float
        get() = unpackFloat1(packedValue)

    /**
     * The _y_ coordinate.
     */
    public val y: Float
        get() = unpackFloat2(packedValue)

    /**
     * @see x
     */
    public operator fun component1(): Float = x

    /**
     * @see y
     */
    public operator fun component2(): Float = y

    /**
     * Copies this [Point], updating one or both of the coordinates. If providing new values for both [x] and [y],
     * consider creating a new [Point] using one of the helper functions instead.
     */
    public fun copy(
        x: Float = this.x,
        y: Float = this.y,
    ): Point = Point(x, y)
}
