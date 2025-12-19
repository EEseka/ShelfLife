package com.eeseka.shelflife.shared.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferencesSerializable(
    val allowed: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int
)