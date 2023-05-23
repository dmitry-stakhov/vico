package v2.core.layout

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import v2.chart.Chart
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.legend.Legend
import extension.isLtr
import v2.axis.AxisManager
import v2.chart.insets.ChartInsetter

public open class VirtualLayout(
    private val axisManager: AxisManager,
) {
    private val tempInsetters = ArrayList<ChartInsetter>(TEMP_INSETTERS_INITIAL_SIZE)

    private val finalInsets: Insets = Insets()

    private val tempInsets: Insets = Insets()

    public open fun <Model : ChartEntryModel> setBounds(
        subcomposeMeasureScope: SubcomposeMeasureScope,
        density: Density,
        layoutDirection: LayoutDirection,
        context: MeasureContext,
        contentBounds: Rect,
        chart: Chart<Model>,
        legend: Legend?,
        segmentProperties: SegmentProperties,
        vararg chartInsetter: ChartInsetter?,
    ): Rect = with(context) {
        tempInsetters.clear()
        finalInsets.clear()
        tempInsets.clear()

        val legendHeight = legend?.getHeight(density, context, contentBounds.width).orZero

        axisManager.addInsetters(tempInsetters)
        chartInsetter.filterNotNull().forEach(tempInsetters::add)
        tempInsetters.addAll(chart.chartInsetters)
        tempInsetters.add(chart)

        tempInsetters.forEach { insetter ->
            insetter.getInsets(subcomposeMeasureScope, density, context, tempInsets, segmentProperties)
            finalInsets.setValuesIfGreater(tempInsets)
        }

        val availableHeight = contentBounds.height - finalInsets.vertical - legendHeight

        tempInsetters.forEach { insetter ->
            insetter.getHorizontalInsets(subcomposeMeasureScope, context, availableHeight, tempInsets)
            finalInsets.setValuesIfGreater(tempInsets)
        }

        val chartBounds = Rect(
            left = contentBounds.left + finalInsets.getLeft(layoutDirection.isLtr),
            top = contentBounds.top + finalInsets.top,
            right = contentBounds.right - finalInsets.getRight(layoutDirection.isLtr),
            bottom = contentBounds.bottom - finalInsets.bottom - legendHeight,
        )

        chart.setBounds(
            left = chartBounds.left,
            top = chartBounds.top,
            right = chartBounds.right,
            bottom = chartBounds.bottom,
        )

        axisManager.setAxesBounds(layoutDirection, contentBounds, chartBounds, finalInsets)

        legend?.setBounds(
            left = contentBounds.left,
            top = chart.bounds.bottom + finalInsets.bottom,
            right = contentBounds.right,
            bottom = chart.bounds.bottom + finalInsets.bottom + legendHeight,
        )

        chartBounds
    }


    private companion object {
        const val TEMP_INSETTERS_INITIAL_SIZE = 5
    }
}