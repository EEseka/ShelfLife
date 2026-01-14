package com.eeseka.shelflife.pantry.presentation.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

sealed interface ExpiryState {
    data class Safe(val value: Int, val unit: CalendarUnit) : ExpiryState // "2 Weeks", "5 Months"
    data class Warning(val days: Int) : ExpiryState // < 7 days
    data class Urgent(val days: Int) : ExpiryState // < 3 days
    data object Today : ExpiryState
    data object Yesterday : ExpiryState
    data class Expired(val value: Int, val unit: CalendarUnit) : ExpiryState
}

@OptIn(ExperimentalTime::class)
fun calculateExpiryState(expiryDate: LocalDate): ExpiryState {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysDiff = today.daysUntil(expiryDate)
    val absDays = abs(daysDiff)

    return when {
        // --- PAST ---
        daysDiff < -1 -> {
            when {
                absDays > 365 -> ExpiryState.Expired(absDays / 365, CalendarUnit.YEAR)
                absDays >= 30 -> ExpiryState.Expired(absDays / 30, CalendarUnit.MONTH)
                else -> ExpiryState.Expired(absDays, CalendarUnit.DAY)
            }
        }
        daysDiff == -1 -> ExpiryState.Yesterday

        // --- PRESENT ---
        daysDiff == 0 -> ExpiryState.Today

        // --- FUTURE ---
        daysDiff <= 3 -> ExpiryState.Urgent(daysDiff)
        daysDiff <= 7 -> ExpiryState.Warning(daysDiff)
        daysDiff > 365 -> ExpiryState.Safe(daysDiff / 365, CalendarUnit.YEAR)
        daysDiff >= 30 -> ExpiryState.Safe(daysDiff / 30, CalendarUnit.MONTH)
        else -> ExpiryState.Safe(daysDiff, CalendarUnit.DAY)
    }
}

enum class CalendarUnit {
    DAY, MONTH, YEAR
}