package com.eeseka.shelflife.shared.domain.settings

import kotlinx.datetime.LocalTime

data class NotificationPreferences(
    val allowed: Boolean = false,
    val reminderTime: LocalTime = LocalTime(hour = 8, minute = 0) // Default 8:00 AM
)