import platform.Foundation.NSCoder
import platform.Foundation.NSNumberFormatter

actual class DecimalFormat actual constructor(pattern: String): NSNumberFormatter(NSCoder()) {
    actual fun formatValue(value: Float): String {
        // TODO
        return ""
    }
}