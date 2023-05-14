package extension

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.LayoutDirection

internal val DrawScope.isLtr: Boolean
    get() = layoutDirection == LayoutDirection.Ltr
