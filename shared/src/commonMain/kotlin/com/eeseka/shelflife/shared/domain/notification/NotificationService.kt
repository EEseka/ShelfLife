package com.eeseka.shelflife.shared.domain.notification

import kotlinx.datetime.LocalTime

interface NotificationService {
    /**
     * Enables or updates the daily reminder time.
     * On Android: Sets the repeating alarm.
     * On iOS: Re-schedules local notifications for all items in LocalStorage.
     */
    suspend fun scheduleDailyNotification(time: LocalTime)

    /**
     * Disables notifications.
     * Android: Cancels the alarm.
     * iOS: Removes all pending requests.
     */
    suspend fun cancelDailyNotification()

    /**
     * Call this when Pantry Data changes (Add/Remove items).
     * Android: No-op (The daily alarm will check DB when it fires).
     * iOS: Re-fetches items from DB and re-schedules the triggers.
     */
    suspend fun refreshNotifications()
}