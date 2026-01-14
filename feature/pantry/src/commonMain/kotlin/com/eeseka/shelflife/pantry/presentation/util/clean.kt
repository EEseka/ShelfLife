package com.eeseka.shelflife.pantry.presentation.util

fun Double.clean(): String {
    return if (this % 1.0 == 0.0) this.toInt().toString() else this.toString()
}
