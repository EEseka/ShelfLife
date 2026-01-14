package com.eeseka.shelflife.shared.data.notification

import com.eeseka.shelflife.shared.domain.database.local.LocalStorageService
import com.eeseka.shelflife.shared.domain.notification.NotificationService
import com.eeseka.shelflife.shared.domain.settings.SettingsService
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalTime

class NativeNotificationService(
    private val scheduler: NotificationScheduler,
    private val localStorage: LocalStorageService,
    private val settingsService: SettingsService
) : NotificationService {

    override suspend fun scheduleDailyNotification(time: LocalTime) {
        // Fetch current data so iOS can schedule
        val items = localStorage.getAllPantryItems().first()
        scheduler.scheduleDaily(time, items)
    }

    override suspend fun cancelDailyNotification() {
        scheduler.cancelDaily()
    }

    override suspend fun refreshNotifications() {
        val prefs = settingsService.notificationPreferences.first()
        if (prefs.allowed) {
            val items = localStorage.getAllPantryItems().first()
            scheduler.scheduleDaily(prefs.reminderTime, items)
        }
    }
}