/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.component.marker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesProvider
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.extension.averageOf
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.marker.DefaultMarkerLabelFormatter
import com.patrykandpatrick.vico.core.marker.Marker
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter
import component.shape.MarkerCorneredShape

/**
 * Creates a [MarkerComponent].
 *
 * @param label the [TextComponent] to use for the label.
 * @param indicator the [Component] to use for the indicator.
 * @param guideline the [LineComponent] to use for the guideline.
 */
@Composable
public fun markerComponent(
    label: TextComponent,
    indicator: Component,
    guideline: LineComponent,
): MarkerComponent = MarkerComponent(
    label = label,
    indicator = indicator,
    guideline = guideline,
)

/**
 * The default implementation of the [Marker] interface.
 *
 * @param label the [TextComponent] used to draw the label.
 * @param indicator an optional indicator drawn at a given point belonging to the data entry.
 * @param guideline an optional line drawn from the bottom of the chart to the bottom edge of the [label].
 */
public open class MarkerComponent(
    public val label: TextComponent,
    public val indicator: Component?,
    public val guideline: LineComponent?,
) : Marker {

    private var tempBounds = Rect.Zero

    private val TextComponent.tickSize: Dp
        get() = ((background as? ShapeComponent)?.shape as? MarkerCorneredShape)?.tickSizeDp.orZero.dp

    /**
     * The indicator size (in dp).
     */
    public var indicatorSizeDp: Float = 0f

    /**
     * An optional lambda function that allows for applying the color associated with a given data entry to a
     * [Component].
     */
    public var onApplyEntryColor: ((entryColor: Int) -> Unit)? = null

    /**
     * The [MarkerLabelFormatter] for this marker.
     */
    public var labelFormatter: MarkerLabelFormatter = DefaultMarkerLabelFormatter

    override fun draw(
        drawScope: DrawScope,
        context: DrawContext,
        bounds: Rect,
        markedEntries: List<Marker.EntryModel>,
        chartValuesProvider: ChartValuesProvider,
    ): Unit = with(context) {
        with(drawScope) {
            drawGuideline(drawScope, bounds, markedEntries)
            val halfIndicatorSize = indicatorSizeDp.half.dp.toPx()

            markedEntries.forEachIndexed { _, model ->
                onApplyEntryColor?.invoke(model.color)
                indicator?.draw(
                    drawScope,
                    model.location.x - halfIndicatorSize,
                    model.location.y - halfIndicatorSize,
                    model.location.x + halfIndicatorSize,
                    model.location.y + halfIndicatorSize,
                )
            }
            drawLabel(
                drawScope,
                context,
                bounds,
                markedEntries,
                chartValuesProvider.getChartValues()
            )
        }
    }

    private fun drawLabel(
        drawScope: DrawScope,
        context: DrawContext,
        bounds: Rect,
        markedEntries: List<Marker.EntryModel>,
        chartValues: ChartValues,
    ): Unit = with(context) {
        with(drawScope) {
            val text = labelFormatter.getLabel(markedEntries, chartValues)
            val entryX = markedEntries.averageOf { it.location.x }
            val labelBounds = Rect.Zero // label.getTextBounds(context, text, outRect = tempBounds)
            val halfOfTextWidth = labelBounds.width.half
            val x = overrideXPositionToFit(entryX, bounds, halfOfTextWidth)
            context[MarkerCorneredShape.tickXKey] = entryX

            label.drawText(
                drawScope = drawScope,
                extras = context,
                text = text,
                textX = x,
                textY = bounds.top - labelBounds.height - label.tickSize.toPx(),
                verticalPosition = Alignment.Bottom,
            )
        }
    }

    private fun overrideXPositionToFit(
        xPosition: Float,
        bounds: Rect,
        halfOfTextWidth: Float,
    ): Float = when {
        xPosition - halfOfTextWidth < bounds.left -> bounds.left + halfOfTextWidth
        xPosition + halfOfTextWidth > bounds.right -> bounds.right - halfOfTextWidth
        else -> xPosition
    }

    private fun drawGuideline(
        drawScope: DrawScope,
        bounds: Rect,
        markedEntries: List<Marker.EntryModel>,
    ) {
        markedEntries
            .map { it.location.x }
            .toSet()
            .forEach { x ->
                guideline?.drawVertical(
                    drawScope,
                    bounds.top,
                    bounds.bottom,
                    x,
                )
            }
    }

    public override fun getInsets(
        density: Density,
        context: MeasureContext,
        outInsets: Insets,
        segmentProperties: SegmentProperties,
    ): Unit = with(density) {
        outInsets.top = label.getHeight(context, density) + label.tickSize.toPx()
    }
}
