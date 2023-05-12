import java.text.DecimalFormat

actual class DecimalFormat actual constructor(pattern: String) : DecimalFormat(pattern) {
    actual fun formatValue(value: Float): String {
        return format(value)
    }
}
