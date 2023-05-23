package v2.chart.insets

import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Density
import com.patrykandpatrick.vico.core.axis.Axis
import v2.chart.Chart
import com.patrykandpatrick.vico.core.chart.insets.HorizontalInsets
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.marker.Marker

/**
 * Enables a component to add insets to [Chart]s to make room for itself. This is used by [Axis], [Marker], and the
 * like.
 */
public interface ChartInsetter {

    /**
     * Called during the measurement phase, before [getHorizontalInsets]. Both horizontal and vertical insets can be
     * requested from this function. The final inset for a given edge of the associated [Chart] is the largest of the
     * insets requested for the edge.
     *
     * @param context holds data used for the measuring of components.
     * @param outInsets used to store the requested insets.
     * @param segmentProperties the associated [Chart]’s [SegmentProperties].
     */
    public fun getInsets(
        subcomposeMeasureScope: SubcomposeMeasureScope,
        density: Density,
        context: MeasureContext,
        outInsets: Insets,
        segmentProperties: SegmentProperties,
    ): Unit = Unit

    /**
     * Called during the measurement phase, after [getInsets]. Only horizontal insets can be requested from this
     * function. Unless the available height is of interest, [getInsets] can be used to set all insets. The final inset
     * for a given edge of the associated [Chart] is the largest of the insets requested for the edge.
     *
     * @param context holds data used for the measuring of components.
     * @param availableHeight the available height. The vertical insets are considered here.
     * @param outInsets used to store the requested insets.
     */
    public fun getHorizontalInsets(
        subcomposeMeasureScope: SubcomposeMeasureScope,
        context: MeasureContext,
        availableHeight: Float,
        outInsets: HorizontalInsets,
    ): Unit = Unit
}