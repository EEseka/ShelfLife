package com.eeseka.shelflife.settings.presentation.util

import kotlinx.datetime.LocalTime

fun LocalTime.formatTo12Hour(amSuffix: String, pmSuffix: String): String {
    val hour12 = if (hour == 0 || hour == 12) 12 else hour % 12
    val minuteStr = minute.toString().padStart(2, '0')
    val suffix = if (hour < 12) amSuffix else pmSuffix
    return "$hour12:$minuteStr $suffix"
}