package com.eeseka.shelflife.shared.data.mappers

import com.eeseka.shelflife.shared.data.dto.NotificationPreferencesSerializable
import com.eeseka.shelflife.shared.domain.settings.NotificationPreferences
import kotlinx.datetime.LocalTime

fun NotificationPreferences.toSerializable(): NotificationPreferencesSerializable {
    return NotificationPreferencesSerializable(
        allowed = allowed,
        reminderHour = reminderTime.hour,
        reminderMinute = reminderTime.minute
    )
}

fun NotificationPreferencesSerializable.toDomain(): NotificationPreferences {
    return NotificationPreferences(
        allowed = allowed,
        reminderTime = LocalTime(hour = reminderHour, minute = reminderMinute)
    )
}