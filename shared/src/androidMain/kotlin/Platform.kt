import java.text.DecimalFormat

public actual class DecimalFormat actual constructor(pattern: String) : DecimalFormat(pattern) {
    public actual fun formatValue(value: Float): String {
        return format(value)
    }
}
