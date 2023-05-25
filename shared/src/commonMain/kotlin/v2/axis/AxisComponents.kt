package v2.axis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.style.currentChartStyle
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.component.shape.DashedShape

@Composable
public fun AxisLabel(
    label: String,
    color: Color = currentChartStyle.axis.axisLabelColor,
    textSize: TextUnit = currentChartStyle.axis.axisLabelTextSize,
    background: Color = Color.Transparent,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    lineCount: Int = currentChartStyle.axis.axisLabelLineCount,
    verticalPadding: Dp = currentChartStyle.axis.axisLabelVerticalPadding,
    horizontalPadding: Dp = currentChartStyle.axis.axisLabelHorizontalPadding,
    verticalMargin: Dp = currentChartStyle.axis.axisLabelVerticalMargin,
    horizontalMargin: Dp = currentChartStyle.axis.axisLabelHorizontalMargin,
    textAlign: TextAlign? = null,
) {
    Text(
        text = label,
        modifier = Modifier
            .padding(
                vertical = verticalMargin,
                horizontal = horizontalMargin,
            )
            .background(background)
            .padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding
            ),
        fontSize = textSize,
        maxLines = DefaultDimens.AXIS_LABEL_MAX_LINES,
        color = color,
        overflow = overflow,
        minLines = lineCount,
        textAlign = textAlign
    )
}

@Composable
public fun VerticalAxisTick(
    width: Dp = 4.dp,
    thickness: Dp = 1.dp,
    color: Color = Color.Black,
) {
    Divider(
        modifier = Modifier.width(width),
        color = color,
        thickness = thickness,
    )
}

@Composable
public fun VerticalAxisLine(
    thickness: Dp = 1.dp,
    color: Color = Color.Black,
) {
    Box(
        Modifier
            .fillMaxHeight()
            .width(thickness)
            .background(color = color)
    )
}

@Composable
public fun VerticalAxisGuideline(
    thickness: Dp = 1.dp,
    color: Color = Color.Black,
    shape: Shape = DashedShape()
) {
    Divider(
        modifier = Modifier.clip(shape),
        thickness = thickness,
        color = color
    )
}

@Composable
public fun HorizontalAxisTick(
    height: Dp = 4.dp,
    thickness: Dp = 1.dp,
    color: Color = Color.Black,
) {
    Box(
        Modifier
            .size(thickness, height)
            .background(color = color)
    )
}

@Composable
public fun HorizontalAxisLine(
    thickness: Dp = 1.dp,
    color: Color = Color.Black,
) {
    Divider(
        color = color,
        thickness = thickness,
    )
}

@Composable
public fun HorizontalAxisGuideline(
    thickness: Dp = 1.dp,
    color: Color = Color.Black,
    shape: Shape = DashedShape(),
) {
    Box(
        Modifier
            .fillMaxHeight()
            .width(thickness)
            .clip(shape)
            .background(color = color)
    )
}
