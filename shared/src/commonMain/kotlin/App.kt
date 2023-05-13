import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.vertical.startAxis
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.legend.VerticalLegend
import kotlin.random.Random

@Composable
public fun App() {
    MaterialTheme {
        val myColor = remember {
            Color.hsv(Random.nextInt(360).toFloat(), 0.75f, 0.75f)
        }

        val opColor = remember {
            Color.hsv(Random.nextInt(360).toFloat(), 0.75f, 0.75f)
        }

        val chartEntryModel = remember {
            entryModelOf(
               listOf(5, 10, 15, 27).withIndex().map { FloatEntry(it.index.toFloat(), it.value.toFloat()) },
                listOf(2, 8, 12, 20).withIndex().map { FloatEntry(it.index.toFloat(), it.value.toFloat()) },
            )
        }

        Chart(
            modifier = Modifier.padding(horizontal = 10.dp),
            chart = lineChart(
                lines = listOf(
                    LineChart.LineSpec(myColor.toArgb()),
                    LineChart.LineSpec(opColor.toArgb()),
                ),
                pointPosition = LineChart.PointPosition.Start,
                axisValuesOverrider = AxisValuesOverrider.fixed(
                    minY = 10f,
                    maxY = 100f
                )
            ),
            model = chartEntryModel,
            legend = VerticalLegend(
                items = listOf(
                    VerticalLegend.Item(
                        icon = ShapeComponent(color = myColor.toArgb()),
                        label = textComponent(color = myColor),
                        labelText = "Dmitry"
                    ),
                    VerticalLegend.Item(
                        icon = ShapeComponent(color = opColor.toArgb()),
                        label = textComponent(color = opColor),
                        labelText = "Morroni"
                    )
                ),
                iconSizeDp = 10f,
                iconPaddingDp = 10f,
            ),
            startAxis = startAxis(
                valueFormatter = DecimalFormatAxisValueFormatter("#")
            ),
            chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false)
        )
    }
}

public expect fun getPlatformName(): String