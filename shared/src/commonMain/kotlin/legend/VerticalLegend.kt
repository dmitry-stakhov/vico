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

package com.patrykandpatrick.vico.core.legend

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.dimension.Padding
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.context.Extras
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.legend.VerticalLegend.Item
import extension.isLtr

/**
 * [VerticalLegend] displays legend items in a vertical list.
 *
 * @param items a [Collection] of [Item]s to be displayed by this [VerticalLegend].
 * @param iconSize defines the size of all [Item.icon]s.
 * @param iconPadding defines the padding between each [Item.icon] and its corresponding [Item.label].
 * @param spacing defines the vertical spacing between each [Item].
 * @param padding defines the padding of the content.
 */
public open class VerticalLegend(
    public var items: Collection<Item>,
    public var iconSize: Dp,
    public var iconPadding: Dp,
    public var spacing: Dp = 0.dp,
    override val padding: MutableDimensions = emptyDimensions(),
) : Legend, Padding {

    private val heights: HashMap<Item, Float> = HashMap()

    override var bounds: Rect = Rect.Zero

    public override fun getHeight(density: Density, extras: Extras, availableWidth: Float): Float =
        with(density) {
            items.fold(0f) { sum, item ->
                sum + maxOf(
                    iconSize.toPx(),
                    item.getHeight(density, extras, availableWidth),
                ).also { height -> heights[item] = height }
            } + (padding.vertical.toPx() + spacing.value * (items.size - 1)).dp.toPx()
        }

    override fun draw(context: ChartDrawContext): Unit = with(context) {
        with(context.drawScope) {
            var currentTop = bounds.top + with(context.drawScope) { padding.top.toPx() }

            items.forEach { item ->

                val height =
                    heights.getOrPut(item) { item.getHeight(drawScope, context, chartBounds.width) }
                val centerY = currentTop + height.half
                var startX = if (isLtr) {
                    chartBounds.left + with(context.drawScope) { padding.start.toPx() }
                } else {
                    chartBounds.right - with(context.drawScope) { padding.start.toPx() } - iconSize.toPx()
                }

                item.icon.draw(
                    drawScope = context.drawScope,
                    left = startX,
                    top = centerY - iconSize.toPx().half,
                    right = startX + iconSize.toPx(),
                    bottom = centerY + iconSize.toPx(),
                )

                startX += if (isLtr) {
                    (iconSize + iconPadding).toPx()
                } else {
                    -iconPadding.toPx()
                }

                item.label.drawText(
                    drawScope = drawScope,
                    extras = context,
                    text = item.labelText,
                    textX = startX,
                    textY = centerY,
                    horizontalPosition = Alignment.End,
                    maxTextWidth = (chartBounds.width - (iconSize + iconPadding + padding.horizontal).toPx())
                        .toInt(),
                )

                currentTop += height + spacing.toPx()
            }
        }
    }

    protected open fun Item.getHeight(
        density: Density,
        extras: Extras,
        availableWidth: Float,
    ): Float = with(density) {
        label.getHeight(
            density = density,
            extras = extras,
            text = labelText,
            width = (availableWidth - iconSize.toPx() - iconPadding.toPx()).toInt(),
        )
    }

    /**
     * Defines the appearance of an item of a [Legend].
     *
     * @param icon the [Component] used as the item’s icon.
     * @param label the [TextComponent] used for the label.
     * @param labelText the text content of the label.
     */
    public class Item(
        public val icon: Component,
        public val label: TextComponent,
        public val labelText: CharSequence,
    )
}
