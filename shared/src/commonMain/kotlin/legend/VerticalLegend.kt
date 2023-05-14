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
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.dimension.Padding
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.context.Extras
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions
import com.patrykandpatrick.vico.core.extension.half

/**
 * [VerticalLegend] displays legend items in a vertical list.
 *
 * @param items a [Collection] of [Item]s to be displayed by this [VerticalLegend].
 * @param iconSizeDp defines the size of all [Item.icon]s.
 * @param iconPaddingDp defines the padding between each [Item.icon] and its corresponding [Item.label].
 * @param spacingDp defines the vertical spacing between each [Item].
 * @param padding defines the padding of the content.
 */
public open class VerticalLegend(
    public var items: Collection<Item>,
    public var iconSizeDp: Float,
    public var iconPaddingDp: Float,
    public var spacingDp: Float = 0f,
    override val padding: MutableDimensions = emptyDimensions(),
) : Legend, Padding {

    private val heights: HashMap<Item, Float> = HashMap()

    override var bounds: Rect = Rect.Zero

    public override fun getHeight(density: Density, extras: Extras, availableWidth: Float): Float = with(density) {
        items.fold(0f) { sum, item ->
            sum + maxOf(
                iconSizeDp.dp.toPx(),
                item.getHeight(density, extras, availableWidth),
            ).also { height -> heights[item] = height }
        } + (padding.vertical.toPx() + spacingDp * (items.size - 1)).dp.toPx()
    }

    override fun draw(context: ChartDrawContext): Unit = with(context) {
        var currentTop = bounds.top + with(context.drawScope) { padding.top.toPx() }

        items.forEach { item ->

            val height = heights.getOrPut(item) { item.getHeight(drawScope, context, chartBounds.width) }
            val centerY = currentTop + height.half
            var startX = if (isLtr) {
                chartBounds.left + with(context.drawScope) { padding.start.toPx() }
            } else {
                chartBounds.right - with(context.drawScope) { padding.start.toPx() } - iconSizeDp.pixels
            }

            item.icon.draw(
                drawScope = context.drawScope,
                left = startX,
                top = centerY - iconSizeDp.half.pixels,
                right = startX + iconSizeDp.pixels,
                bottom = centerY + iconSizeDp.half.pixels,
            )

            startX += if (isLtr) {
                (iconSizeDp + iconPaddingDp).pixels
            } else {
                -iconPaddingDp.pixels
            }

            item.label.drawText(
                drawScope = drawScope,
                extras = context,
                text = item.labelText,
                textX = startX,
                textY = centerY,
                horizontalPosition = Alignment.End,
                maxTextWidth = (chartBounds.width - (iconSizeDp + iconPaddingDp + padding.horizontal.value).pixels)
                    .toInt(),
            )

            currentTop += height + spacingDp.pixels
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
            width = (availableWidth - iconSizeDp.dp.toPx() - iconPaddingDp.dp.toPx()).toInt(),
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
