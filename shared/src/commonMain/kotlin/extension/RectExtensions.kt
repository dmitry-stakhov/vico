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

import androidx.compose.ui.geometry.Rect

/**
 * Returns [RectF.left] if [isLtr] is true, and [RectF.right] otherwise.
 */
public fun Rect.getStart(isLtr: Boolean): Float = if (isLtr) left else right

/**
 * Returns [RectF.right] if [isLtr] is true, and [RectF.left] otherwise.
 */
public fun Rect.getEnd(isLtr: Boolean): Float = if (isLtr) right else left
