package v2.core.layout

import com.patrykandpatrick.vico.core.chart.insets.ChartInsetter
import com.patrykandpatrick.vico.core.chart.insets.Insets
import v2.axis.AxisManager

public open class VirtualLayout(
    private val axisManager: AxisManager,
) {
    private val tempInsetters = ArrayList<ChartInsetter>(TEMP_INSETTERS_INITIAL_SIZE)

    private val finalInsets: Insets = Insets()

    private val tempInsets: Insets = Insets()

    private companion object {
        const val TEMP_INSETTERS_INITIAL_SIZE = 5
    }
}