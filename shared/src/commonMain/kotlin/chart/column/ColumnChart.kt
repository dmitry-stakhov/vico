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

package com.patrykandpatrick.vico.core.chart.column

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.AxisRenderer
import com.patrykandpatrick.vico.core.chart.BaseChart
import com.patrykandpatrick.vico.core.chart.composed.ComposedChart
import com.patrykandpatrick.vico.core.chart.decoration.Decoration
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.draw.segmentWidth
import com.patrykandpatrick.vico.core.chart.forEachInIndexed
import com.patrykandpatrick.vico.core.chart.put
import com.patrykandpatrick.vico.core.chart.segment.MutableSegmentProperties
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.component.text.inBounds
import com.patrykandpatrick.vico.core.component.text.negative
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.getRepeating
import com.patrykandpatrick.vico.core.extension.getStart
import com.patrykandpatrick.vico.core.extension.half
import com.patrykandpatrick.vico.core.formatter.DecimalFormatValueFormatter
import com.patrykandpatrick.vico.core.formatter.ValueFormatter
import com.patrykandpatrick.vico.core.marker.Marker
import extension.isLtr
import extension.layoutDirectionMultiplier
import kotlin.math.abs

/**
 * [ColumnChart] displays data as vertical bars. It can draw multiple columns per segment.
 *
 * @param columns the [LineComponent] instances to use for columns. This list is iterated through as many times
 * as necessary for each chart segment. If the list contains a single element, all columns have the same appearance.
 * @param spacing the horizontal padding between the edges of chart segments and the columns they contain.
 * @param innerSpacing the spacing between the columns contained in chart segments. This has no effect on
 * segments that contain a single column only.
 * @param mergeMode defines how columns should be drawn in multi-column segments.
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the top of their
 * respective columns.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels (in degrees).
 */
public open class ColumnChart(
    public var columns: List<LineComponent>,
    public var spacing: Dp = DefaultDimens.COLUMN_OUTSIDE_SPACING.dp,
    public var innerSpacing: Dp = DefaultDimens.COLUMN_INSIDE_SPACING.dp,
    public var mergeMode: MergeMode = MergeMode.Grouped,
    public var targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    public var dataLabel: TextComponent? = null,
    public var dataLabelVerticalPosition: Alignment.Vertical = Alignment.Top,
    public var dataLabelValueFormatter: ValueFormatter = DecimalFormatValueFormatter(),
    public var dataLabelRotationDegrees: Float = 0f,
) : BaseChart<ChartEntryModel>() {

    /**
     * Creates a [ColumnChart] with a common style for all columns.
     *
     * @param column a [LineComponent] defining the appearance of the columns.
     * @param spacing the horizontal padding between the edges of chart segments and the columns they contain.
     * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
     * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
     */
    public constructor(
        column: LineComponent,
        spacing: Dp = DefaultDimens.COLUMN_OUTSIDE_SPACING.dp,
        targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    ) : this(
        columns = listOf(column),
        spacing = spacing,
        targetVerticalAxisPosition = targetVerticalAxisPosition
    )

    /**
     * Creates a [ColumnChart] instance with [columns] set to an empty list. The list must be populated before the chart
     * is drawn.
     */
    public constructor() : this(emptyList())

    /**
     * When [mergeMode] is set to [MergeMode.Stack], this maps the x-axis value of every non-empty segment to a pair
     * containing the bottom coordinate of the segment’s bottommost column and the top coordinate of the segment’s
     * topmost column. This hash map is used by [drawChart] and [drawChartInternal].
     */
    protected val heightMap: HashMap<Float, Pair<Float, Float>> = HashMap()

    /**
     * The chart’s [MutableSegmentProperties] instance, which holds information about the segment properties.
     */
    protected val segmentProperties: MutableSegmentProperties = MutableSegmentProperties()

    override val entryLocationMap: HashMap<Float, MutableList<Marker.EntryModel>> = HashMap()

    public override fun drawChart(
        drawScope: DrawScope,
        context: ChartDrawContext,
        model: ChartEntryModel,
    ): Unit = with(context) {
        entryLocationMap.clear()
        drawChartInternal(
            drawScope = drawScope,
            chartValues = chartValuesManager.getChartValues(axisPosition = targetVerticalAxisPosition),
            model = model,
            cellWidth = segmentProperties.cellWidth,
            spacing = segmentProperties.marginWidth,
        )
        heightMap.clear()
    }

    protected open fun ChartDrawContext.drawChartInternal(
        drawScope: DrawScope,
        chartValues: ChartValues,
        model: ChartEntryModel,
        cellWidth: Float,
        spacing: Float,
    ) {
        val yRange = (chartValues.maxY - chartValues.minY).takeIf { it != 0f } ?: return
        val heightMultiplier: Float = bounds.height / yRange

        var drawingStart: Float
        var height: Float
        var columnCenterX: Float
        var column: LineComponent
        var columnTop: Float
        var columnBottom: Float
        val zeroLinePosition = bounds.bottom + chartValues.minY / yRange * bounds.height

        val defCellWidth = Density(density).getCellWidth(model.entries.size) * chartScale

        model.entries.forEachIndexed { index, entryCollection ->

            column = columns.getRepeating(index)
            drawingStart = getDrawingStart(
                drawScope = drawScope,
                entryCollectionIndex = index,
                segmentCompensation = (cellWidth - defCellWidth) / 2,
                spacing = spacing,
                columnWidth = with(drawScope) { column.thickness.toPx() } * chartScale,
            ) - horizontalScroll

            entryCollection.forEachInIndexed(range = chartValues.minX..chartValues.maxX) { entryIndex, entry ->

                height = abs(entry.y) * heightMultiplier
                columnCenterX = drawingStart + drawScope.layoutDirectionMultiplier *
                        (cellWidth + spacing) * (entry.x - chartValues.minX) / model.xStep

                when (mergeMode) {
                    MergeMode.Stack -> {
                        val (stackedNegY, stackedPosY) = heightMap.getOrElse(entry.x) { 0f to 0f }
                        columnBottom = zeroLinePosition +
                                if (entry.y < 0f) {
                                    height + abs(stackedNegY) * heightMultiplier
                                } else {
                                    -stackedPosY * heightMultiplier
                                }

                        columnTop = (columnBottom - height).coerceAtMost(columnBottom)
                        columnCenterX += drawScope.layoutDirectionMultiplier * cellWidth.half
                        heightMap[entry.x] =
                            if (entry.y < 0f) {
                                stackedNegY + entry.y to stackedPosY
                            } else {
                                stackedNegY to stackedPosY + entry.y
                            }
                    }

                    MergeMode.Grouped -> {
                        columnBottom = zeroLinePosition + if (entry.y < 0f) height else 0f
                        columnTop = columnBottom - height
                        columnCenterX += drawScope.layoutDirectionMultiplier * with(drawScope) { column.thickness.toPx() } * chartScale
                    }
                }

                val columnSignificantY = if (entry.y < 0f) columnBottom else columnTop

                if (column.intersectsVertical(
                        context = this,
                        top = columnTop,
                        bottom = columnBottom,
                        centerX = columnCenterX,
                        boundingBox = bounds,
                        thicknessScale = chartScale,
                    )
                ) {
                    updateMarkerLocationMap(
                        entry,
                        columnSignificantY,
                        columnCenterX,
                        column,
                        entryIndex
                    )
                    column.drawVertical(
                        drawScope,
                        columnTop,
                        columnBottom,
                        columnCenterX,
                        chartScale
                    )
                }

                if (mergeMode == MergeMode.Grouped) {
                    drawDataLabel(
                        drawScope,
                        model.entries.size,
                        column.thickness,
                        entry.y,
                        columnCenterX,
                        columnSignificantY
                    )
                } else if (entryIndex == model.entries.lastIndex) {
                    val yValues = heightMap[entry.x]
                    drawStackedDataLabel(
                        drawScope,
                        model.entries.size,
                        column.thickness,
                        yValues?.first,
                        yValues?.second,
                        columnCenterX,
                        zeroLinePosition,
                        heightMultiplier,
                    )
                }
            }
        }
    }

    protected open fun ChartDrawContext.drawStackedDataLabel(
        drawScope: DrawScope,
        modelEntriesSize: Int,
        columnThickness: Dp,
        negativeY: Float?,
        positiveY: Float?,
        x: Float,
        zeroLinePosition: Float,
        heightMultiplier: Float,
    ) {
        if (positiveY != null && positiveY > 0f) {
            val y = zeroLinePosition - positiveY * heightMultiplier
            drawDataLabel(drawScope, modelEntriesSize, columnThickness, positiveY, x, y)
        }
        if (negativeY != null && negativeY < 0f) {
            val y = zeroLinePosition + abs(negativeY) * heightMultiplier
            drawDataLabel(drawScope, modelEntriesSize, columnThickness, negativeY, x, y)
        }
    }

    protected open fun ChartDrawContext.drawDataLabel(
        drawScope: DrawScope,
        modelEntriesSize: Int,
        columnThickness: Dp,
        dataLabelValue: Float,
        x: Float,
        y: Float,
    ) {
        dataLabel?.let { textComponent ->

            val canUseSegmentWidth =
                mergeMode == MergeMode.Stack ||
                        mergeMode == MergeMode.Grouped && modelEntriesSize == 1
            val maxWidth = when {
                canUseSegmentWidth -> segmentWidth(drawScope)
                mergeMode == MergeMode.Grouped ->
                    with(drawScope) {
                        (columnThickness.value + 2 * minOf(
                            spacing.value,
                            innerSpacing.value.half
                        )).dp.toPx().toInt()
                    }

                else -> error(message = "Encountered an unexpected `MergeMode`.")
            } * chartScale
            val text = dataLabelValueFormatter.formatValue(
                value = dataLabelValue,
                chartValues = chartValuesManager.getChartValues(axisPosition = targetVerticalAxisPosition),
            )
            val dataLabelWidth = textComponent.getWidth(
                context = this,
                text = text,
                rotationDegrees = dataLabelRotationDegrees,
            ).coerceAtMost(maximumValue = maxWidth)

            if (x - dataLabelWidth.half > bounds.right || x + dataLabelWidth.half < bounds.left) return

            val labelVerticalPosition =
                if (dataLabelValue < 0f) dataLabelVerticalPosition.negative() else dataLabelVerticalPosition

            val verticalPosition = labelVerticalPosition.inBounds(
                y = y,
                bounds = bounds,
                componentHeight = textComponent.getHeight(
                    extras = this,
                    density = Density(density),
                    text = text,
                    width = maxWidth.toInt(),
                    rotationDegrees = dataLabelRotationDegrees,
                ),
            )
            textComponent.drawText(
                drawScope = drawScope,
                extras = this,
                text = text,
                textX = x,
                textY = y,
                verticalPosition = verticalPosition,
                maxTextWidth = maxWidth.toInt(),
                rotationDegrees = dataLabelRotationDegrees,
            )
        }
    }

    protected open fun updateMarkerLocationMap(
        entry: ChartEntry,
        columnTop: Float,
        columnCenterX: Float,
        column: LineComponent,
        index: Int,
    ) {
        if (columnCenterX in bounds.left..bounds.right) {
            entryLocationMap.put(
                x = columnCenterX,
                y = columnTop.coerceIn(bounds.top, bounds.bottom),
                entry = entry,
                color = Color.Companion.Black.toArgb(), // column.color,
                index = index,
            )
        }
    }

    override fun updateChartValues(chartValuesManager: ChartValuesManager, model: ChartEntryModel) {
        chartValuesManager.tryUpdate(
            minX = axisValuesOverrider?.getMinX(model) ?: model.minX,
            maxX = axisValuesOverrider?.getMaxX(model) ?: model.maxX,
            minY = axisValuesOverrider?.getMinY(model) ?: mergeMode.getMinY(model),
            maxY = axisValuesOverrider?.getMaxY(model) ?: mergeMode.getMaxY(model),
            chartEntryModel = model,
            axisPosition = targetVerticalAxisPosition,
        )
    }

    override fun getSegmentProperties(
        density: Density,
        model: ChartEntryModel,
    ): SegmentProperties = with(density) {
        segmentProperties.set(
            cellWidth = getCellWidth(model.entries.size),
            marginWidth = spacing.toPx()
        )
    }

    protected open fun Density.getCellWidth(
        entryCollectionSize: Int,
    ): Float = when (mergeMode) {
        MergeMode.Stack ->
            columns.maxOf { it.thickness.toPx() }

        MergeMode.Grouped ->
            getCumulatedThickness(entryCollectionSize) + innerSpacing.toPx() * (entryCollectionSize - 1)
    }

    protected open fun MeasureContext.getDrawingStart(
        drawScope: DrawScope,
        entryCollectionIndex: Int,
        segmentCompensation: Float,
        columnWidth: Float,
        spacing: Float,
    ): Float = with(drawScope) {
        val baseStart =
            bounds.getStart(isLtr = isLtr) + layoutDirectionMultiplier * spacing.half
        when (mergeMode) {
            MergeMode.Stack -> baseStart
            MergeMode.Grouped -> {
                val offset = segmentCompensation - columnWidth.half +
                        Density(density).getCumulatedThickness(entryCollectionIndex) * chartScale +
                        innerSpacing.toPx() * chartScale * entryCollectionIndex
                baseStart + layoutDirectionMultiplier * offset
            }
        }
    }

    protected open fun Density.getCumulatedThickness(count: Int): Float {
        var thickness = 0f
        for (i in 0 until count) {
            thickness += columns.getRepeating(i).thickness.value * this.density
        }
        return thickness
    }

    /**
     * Defines how a [ColumnChart] should draw columns in multi-column segments.
     */
    public enum class MergeMode {

        /**
         * Columns with the same x-axis values will be placed next to each other in groups.
         */
        Grouped,

        /**
         * Columns with the same x-axis values will be placed on top of each other.
         */
        Stack,

        ;

        /**
         * Returns the minimum y-axis value, taking into account the current [MergeMode].
         */
        public fun getMinY(model: ChartEntryModel): Float = when (this) {
            Grouped -> model.minY.coerceAtMost(0f)
            Stack -> model.stackedNegativeY.coerceAtMost(0f)
        }

        /**
         * Returns the maximum y-axis value, taking into account the current [MergeMode].
         */
        public fun getMaxY(model: ChartEntryModel): Float = when (this) {
            Grouped -> model.maxY
            Stack -> model.stackedPositiveY
        }
    }
}

/**
 * Creates a [ColumnChart].
 *
 * @param columns the [LineComponent] instances to use for columns. This list is iterated through as many times
 * as necessary for each chart segment. If the list contains a single element, all columns have the same appearance.
 * @param spacing the horizontal padding between the edges of chart segments and the columns they contain.
 * @param innerSpacing the spacing between the columns contained in chart segments. This has no effect on
 * segments that contain a single column only.
 * @param mergeMode defines how columns should be drawn in multi-column segments.
 * @param decorations the list of [Decoration]s that will be added to the [ColumnChart].
 * @param persistentMarkers maps x-axis values to persistent [Marker]s.
 * @param dataLabel an optional [TextComponent] to use for data labels.
 * @param dataLabelVerticalPosition the vertical position of data labels relative to the top of their
 * respective columns.
 * @param dataLabelValueFormatter the [ValueFormatter] to use for data labels.
 * @param dataLabelRotationDegrees the rotation of data labels (in degrees).
 * @param axisValuesOverrider overrides the minimum and maximum x-axis and y-axis values.
 * @param targetVerticalAxisPosition if this is set, any [AxisRenderer] with an [AxisPosition] equal to the provided
 * value will use the [ChartValues] provided by this chart. This is meant to be used with [ComposedChart].
 *
 * @see Chart
 * @see ColumnChart
 */
@Composable
public fun columnChart(
    columns: List<LineComponent> = currentChartStyle.columnChart.columns,
    spacing: Dp = currentChartStyle.columnChart.outsideSpacing,
    innerSpacing: Dp = currentChartStyle.columnChart.innerSpacing,
    mergeMode: ColumnChart.MergeMode = currentChartStyle.columnChart.mergeMode,
    decorations: List<Decoration>? = null,
    persistentMarkers: Map<Float, Marker>? = null,
    targetVerticalAxisPosition: AxisPosition.Vertical? = null,
    dataLabel: TextComponent? = currentChartStyle.columnChart.dataLabel,
    dataLabelVerticalPosition: Alignment.Vertical = currentChartStyle.columnChart.dataLabelVerticalPosition,
    dataLabelValueFormatter: ValueFormatter = currentChartStyle.columnChart.dataLabelValueFormatter,
    dataLabelRotationDegrees: Float = currentChartStyle.columnChart.dataLabelRotationDegrees,
    axisValuesOverrider: AxisValuesOverrider<ChartEntryModel>? = null,
): ColumnChart = remember { ColumnChart() }.apply {
    this.columns = columns
    this.spacing = spacing
    this.innerSpacing = innerSpacing
    this.mergeMode = mergeMode
    this.dataLabel = dataLabel
    this.dataLabelVerticalPosition = dataLabelVerticalPosition
    this.dataLabelValueFormatter = dataLabelValueFormatter
    this.dataLabelRotationDegrees = dataLabelRotationDegrees
    this.axisValuesOverrider = axisValuesOverrider
    this.targetVerticalAxisPosition = targetVerticalAxisPosition
    decorations?.also(::setDecorations)
    persistentMarkers?.also(::setPersistentMarkers)
}
