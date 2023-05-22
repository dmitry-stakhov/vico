package v2.formatter

public object IntFormatter {
    public fun format(float: Float): String = float.toInt().toString()
}
