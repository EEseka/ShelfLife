package com.eeseka.shelflife.shared.data.notification

import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import kotlinx.datetime.LocalTime

expect class NotificationScheduler {
    /**
     * Schedules the daily notification logic.
     * @param time The user's preferred time (e.g. 08:00).
     * @param items The FULL list of pantry items from the database.
     * - Android: Ignores this list (queries DB when alarm fires).
     * - iOS: Uses this list to schedule future local notifications.
     */
    suspend fun scheduleDaily(time: LocalTime, items: List<PantryItem>)

    /**
     * Cancels all scheduled notifications.
     */
    fun cancelDaily()
}