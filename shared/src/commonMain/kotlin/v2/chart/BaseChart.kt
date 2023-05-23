package v2.chart

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.patrykandpatrick.vico.core.chart.decoration.Decoration
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.dimensions.BoundsAware
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.marker.Marker
import v2.chart.insets.ChartInsetter

public abstract class BaseChart<in Model : ChartEntryModel> : Chart<Model>, BoundsAware {
    private val decorations = ArrayList<Decoration>()

    private val insets: Insets = Insets()

    /**
     * A [HashMap] that links x-axis values to [Marker]s.
     */
    protected val persistentMarkers: HashMap<Float, ChartInsetter> = HashMap()

    override var bounds: Rect = Rect.Zero

    override val chartInsetters: Collection<ChartInsetter> = persistentMarkers.values

    override var axisValuesOverrider: AxisValuesOverrider<@UnsafeVariance Model>? = null

    protected abstract fun drawChart(
        drawChart: DrawScope,
        context: ChartDrawContext,
        model: Model,
    )
}