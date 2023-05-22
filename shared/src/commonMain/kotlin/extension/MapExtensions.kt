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

package com.patrykandpatrick.vico.core.extension

import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.model.Point

/**
 * Returns that of the [Marker.EntryModel]s stored in the [Map] whose x-axis map key is the closest to the [Point.x]
 * value.
 *
 * @see Marker.EntryModel
 */
public fun Map<Float, List<Marker.EntryModel>>.getClosestMarkerEntryModel(
    touchPoint: Point,
): List<Marker.EntryModel>? = keys.findClosestPositiveValue(touchPoint.x)?.let(::get)

/**
 * Returns those of the [Marker.EntryModel]s stored in the [Map] whose [ChartEntry.x] is equal to [xValue].
 *
 * @see Marker.EntryModel
 */
public fun Map<Float, List<Marker.EntryModel>>.getEntryModel(
    xValue: Float,
): List<Marker.EntryModel>? = values
    .mapNotNull { entries -> entries.takeIf { it.firstOrNull()?.entry?.x == xValue } }
    .flatten()
    .takeIf { it.isNotEmpty() }

/**
 * Updates the receiver [TreeMap] with the contents of another [Map].
 */
public fun <K, V> MutableMap<K, MutableList<V>>.updateAll(other: Map<K, List<V>>) {
    other.forEach { (key, value) ->
        put(key, get(key)?.apply { addAll(value) } ?: mutableListOf(value))
    }
}

internal inline fun <K, V> HashMap<K, MutableList<V>>.updateList(
    key: K,
    initialCapacity: Int = 0,
    block: MutableList<V>.() -> Unit,
) {
    block(getOrPut(key) { ArrayList(initialCapacity) })
}
