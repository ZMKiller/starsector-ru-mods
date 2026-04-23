package SOTFECO

import kotlin.math.round

object SOTFECO_MathUtils {
    fun Float.trimHangingZero(): Number {
        if (this % 1 == 0f) return this.toInt()
        return this
    }

    fun Double.roundNumTo(decimalPoints: Int): Double {
        var multiplier = 1.0
        repeat(decimalPoints) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    fun Float.roundNumTo(decimalPoints: Int): Float {
        return this.toDouble().roundNumTo(decimalPoints).toFloat()
    }
}