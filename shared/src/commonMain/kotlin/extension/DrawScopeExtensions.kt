package extension

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.LayoutDirection

internal val DrawScope.isLtr: Boolean
    get() = layoutDirection == LayoutDirection.Ltr

internal val LayoutDirection.isLtr: Boolean
    get() = this == LayoutDirection.Ltr

/**
 * A multiplier used to ensure support for both left-to-right and right-to-left layouts. Values such as translation
 * deltas are multiplied by this value. [layoutDirectionMultiplier] is equal to `1f` if [isLtr] is `true`, and `-1f`
 * otherwise.
 */
internal val DrawScope.layoutDirectionMultiplier: Float
    get() = if (isLtr) 1f else -1f

internal val LayoutDirection.layoutDirectionMultiplier: Float
    get() = if (isLtr) 1f else -1f
