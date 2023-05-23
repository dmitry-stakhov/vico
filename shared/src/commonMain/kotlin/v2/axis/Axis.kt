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

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.axis.formatter.DefaultAxisValueFormatter
import com.patrykandpatrick.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatrick.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.setAll

/**
 * A basic implementation of [AxisRenderer] used throughout the library.
 *
 * @see AxisRenderer
 * @see HorizontalAxis
 * @see VerticalAxis
 */
public abstract class Axis<Position : AxisPosition> : AxisRenderer<Position> {

    private val restrictedBounds: MutableList<Rect> = mutableListOf()

    protected val labels: ArrayList<CharSequence> = ArrayList()

    override var bounds: Rect = Rect.Zero

    protected val Density.axisThickness: Float
        get() = axisLine?.thickness?.toPx().orZero

    protected val Density.tickThickness: Float
        get() = tick?.thickness?.toPx().orZero

    protected val Density.guidelineThickness: Float
        get() = guideline?.thickness?.toPx().orZero

    protected val Density.tickLengthPx: Float
        get() = if (tick != null) tickLength.toPx() else 0f

    /**
     * The [TextComponent] to use for labels.
     */
    public abstract val label: @Composable (label: String) -> Unit

    /**
     * The [LineComponent] to use for the axis line.
     */
    public var axisLine: LineComponent? = null

    /**
     * The [LineComponent] to use for ticks.
     */
    public var tick: LineComponent? = null

    /**
     * The [LineComponent] to use for guidelines.
     */
    public var guideline: LineComponent? = null

    /**
     * The tick length (in dp).
     */
    public var tickLength: Dp = 0.dp

    /**
     * Used by [Axis] subclasses for sizing and layout.
     */
    public var sizeConstraint: SizeConstraint = SizeConstraint.Auto()

    /**
     * The [AxisValueFormatter] for the axis.
     */
    public var valueFormatter: AxisValueFormatter<Position> = DefaultAxisValueFormatter()

    /**
     * The rotation of axis labels (in degrees).
     */
    public var labelRotationDegrees: Float = 0f

    /**
     * An optional [TextComponent] to use as the axis title.
     */
    public var titleComponent: TextComponent? = null

    /**
     * The axis title.
     */
    public var title: CharSequence? = null

    override fun setRestrictedBounds(vararg bounds: Rect?) {
        restrictedBounds.setAll(bounds.filterNotNull())
    }

    protected fun isNotInRestrictedBounds(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Boolean = restrictedBounds.none {
        it.contains(Offset(right - left, bottom - top)) || it.overlaps(
            Rect(
                left,
                top,
                right,
                bottom
            )
        )
    }

    /**
     * Defines how an [Axis] is to size itself.
     * - For [VerticalAxis], this defines the width.
     * - For [HorizontalAxis], this defines the height.
     *
     * @see [VerticalAxis]
     * @see [HorizontalAxis]
     */
    public sealed class SizeConstraint {

        /**
         * The axis will measure itself and use as much space as it needs, but no less than [minSize], and no more
         * than [maxSize].
         */
        public class Auto(
            public val minSize: Dp = 0.dp,
            public val maxSize: Dp = Float.MAX_VALUE.dp,
        ) : SizeConstraint()

        /**
         * The axis size will be exactly [size].
         */
        public class Exact(public val size: Dp) : SizeConstraint()

        /**
         * The axis will use a fraction of the available space.
         *
         * @property fraction the fraction of the available space that the axis should use.
         */
        public class Fraction(public val fraction: Float) : SizeConstraint() {
            init {
                require(fraction in MIN..MAX) { "Expected a value in the interval [$MIN, $MAX]. Got $fraction." }
            }

            private companion object {
                const val MIN = 0f
                const val MAX = 0.5f
            }
        }

        /**
         * The axis will measure the width of its label component ([label]) for the given [String] ([text]), and it will
         * use this width as its size. In the case of [VerticalAxis], the width of the axis line and the tick length
         * will also be considered.
         */
        public class TextWidth(public val text: String) : SizeConstraint()
    }
}
