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

package v2.axis

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.context.MeasureContext
import v2.chart.Chart
import com.patrykandpatrick.vico.core.dimensions.BoundsAware
import v2.chart.insets.ChartInsetter

/**
 * Defines the minimal set of properties and functions required by other parts of the library to draw an axis.
 */
public interface AxisRenderer<Position : AxisPosition> : BoundsAware, ChartInsetter {

    /**
     * Defines the position of the axis relative to the [Chart].
     */
    public val position: Position

    public fun getPlaceables(
        measureScope: SubcomposeMeasureScope,
        measureContext: MeasureContext,
        chartSize: IntSize,
    ) : AxisPlaceables

    public fun Placeable.PlacementScope.placeAxis(
        layoutDirection: LayoutDirection,
        axisLine: Placeable,
        axisOffset: Int,
        axisLabelPlaceables: List<Placeable>,
        tickPlaceables: List<Placeable>,
        guidelinePlaceables: List<Placeable>,
        constraints: Constraints,
    )

    /**
     * The bounds ([Rect]) passed here define the area where the [AxisRenderer] shouldn’t draw anything.
     */
    public fun setRestrictedBounds(vararg bounds: Rect?)
}
