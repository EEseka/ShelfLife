package com.eeseka.shelflife.pantry.presentation.util

import kotlin.math.abs
import kotlin.math.roundToInt

fun Double.clean(): String {
    val roundedHundredths = (this * 100).roundToInt()
    val wholePart = roundedHundredths / 100
    val remainder = abs(roundedHundredths % 100)

    return if (remainder == 0) {
        wholePart.toString()
    } else {
        val decimalString = remainder.toString().trimEnd('0')
        "$wholePart.$decimalString"
    }
}
