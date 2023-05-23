package v2.axis

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.core.axis.AxisManager
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.insets.Insets
import extension.isLtr
import v2.chart.insets.ChartInsetter

public open class AxisManager(
    /**
     * The [AxisRenderer] for the start axis.
     */
    public val startAxis: AxisRenderer<AxisPosition.Vertical.Start>? = null,

    /**
     * The [AxisRenderer] for the top axis.
     */
    public val topAxis: AxisRenderer<AxisPosition.Horizontal.Top>? = null,

    /**
     * The [AxisRenderer] for the end axis.
     */
    public val endAxis: AxisRenderer<AxisPosition.Vertical.End>? = null,

    /**
     * The [AxisRenderer] for the bottom axis.
     */
    public val bottomAxis: AxisRenderer<AxisPosition.Horizontal.Bottom>? = null,
) {
    private val axisCache = listOf(startAxis, topAxis, endAxis, bottomAxis)
    /**
     * Adds the [AxisRenderer]s controlled by this [AxisManager] to the given [MutableList] of [ChartInsetter]s.
     *
     * @see ChartInsetter
     */
    public fun addInsetters(destination: MutableList<ChartInsetter>) {
        startAxis?.let(destination::add)
        topAxis?.let(destination::add)
        endAxis?.let(destination::add)
        bottomAxis?.let(destination::add)
    }

    public fun setAxesBounds(
        layoutDirection: LayoutDirection,
        contentBounds: Rect,
        chartBounds: Rect,
        insets: Insets,
    ) {
        startAxis?.setStartAxisBounds(
            layoutDirection = layoutDirection,
            contentBounds = contentBounds,
            chartBounds = chartBounds,
            insets = insets,
        )

        topAxis?.setTopAxisBounds(
            layoutDirection = layoutDirection,
            contentBounds = contentBounds,
            insets = insets,
        )

        endAxis?.setEndAxisBounds(
            layoutDirection = layoutDirection,
            contentBounds = contentBounds,
            chartBounds = chartBounds,
            insets = insets,
        )

        bottomAxis?.setBottomAxisBounds(
            layoutDirection = layoutDirection,
            contentBounds = contentBounds,
            chartBounds = chartBounds,
            insets = insets,
        )

        setRestrictedBounds()
    }

    private fun AxisRenderer<AxisPosition.Vertical.Start>.setStartAxisBounds(
        layoutDirection: LayoutDirection,
        contentBounds: Rect,
        chartBounds: Rect,
        insets: Insets,
    ) {
        setBounds(
            left = if (layoutDirection.isLtr) contentBounds.left else contentBounds.right - insets.start,
            top = chartBounds.top,
            right = if (layoutDirection.isLtr) contentBounds.left + insets.start else contentBounds.right,
            bottom = chartBounds.bottom,
        )
    }

    private fun AxisRenderer<AxisPosition.Horizontal.Top>.setTopAxisBounds(
        layoutDirection: LayoutDirection,
        contentBounds: Rect,
        insets: Insets,
    ) {
        setBounds(
            left = contentBounds.left + if (layoutDirection.isLtr) insets.start else insets.end,
            top = contentBounds.top,
            right = contentBounds.right - if (layoutDirection.isLtr) insets.end else insets.start,
            bottom = contentBounds.top + insets.top,
        )
    }

    private fun AxisRenderer<AxisPosition.Vertical.End>.setEndAxisBounds(
        layoutDirection: LayoutDirection,
        contentBounds: Rect,
        chartBounds: Rect,
        insets: Insets,
    ) {
        setBounds(
            left = if (layoutDirection.isLtr) contentBounds.right - insets.end else contentBounds.left,
            top = chartBounds.top,
            right = if (layoutDirection.isLtr) contentBounds.right else contentBounds.left + insets.end,
            bottom = chartBounds.bottom,
        )
    }

    private fun AxisRenderer<AxisPosition.Horizontal.Bottom>.setBottomAxisBounds(
        layoutDirection: LayoutDirection,
        contentBounds: Rect,
        chartBounds: Rect,
        insets: Insets,
    ) {
        setBounds(
            left = contentBounds.left + if (layoutDirection.isLtr) insets.start else insets.end,
            top = chartBounds.bottom,
            right = contentBounds.right - if (layoutDirection.isLtr) insets.end else insets.start,
            bottom = chartBounds.bottom + insets.bottom,
        )
    }

    private fun setRestrictedBounds() {
        startAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
        topAxis?.setRestrictedBounds(startAxis?.bounds, endAxis?.bounds, bottomAxis?.bounds)
        endAxis?.setRestrictedBounds(topAxis?.bounds, startAxis?.bounds, bottomAxis?.bounds)
        bottomAxis?.setRestrictedBounds(topAxis?.bounds, endAxis?.bounds, startAxis?.bounds)
    }

    public fun placeAxis(
        scope: Placeable.PlacementScope,
        axisLine: Placeable,
        axisOffset: Int,
        axisLabelPlaceables: List<Placeable>,
        tickPlaceables: List<Placeable>,
        constraints: Constraints,
    ) {
        axisCache.forEach { axis ->
            if (axis != null) {
                with(axis) {
                    scope.placeAxis(
                        axisLine = axisLine,
                        axisOffset = axisOffset,
                        axisLabelPlaceables = axisLabelPlaceables,
                        tickPlaceables = tickPlaceables,
                        constraints = constraints,
                    )
                }
            }
        }
    }
}
