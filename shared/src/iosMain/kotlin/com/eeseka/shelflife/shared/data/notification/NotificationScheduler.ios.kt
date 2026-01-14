package com.eeseka.shelflife.shared.data.notification

import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import shelflife.shared.generated.resources.Res
import shelflife.shared.generated.resources.notification_many_items
import shelflife.shared.generated.resources.notification_single_item
import shelflife.shared.generated.resources.notification_title
import shelflife.shared.generated.resources.notification_two_items

actual class NotificationScheduler {

    actual suspend fun scheduleDaily(time: LocalTime, items: List<PantryItem>) {
        cancelDaily()

        // Group items by Expiry Date
        val grouped = items.groupBy { it.expiryDate }

        // Iterate and Schedule
        grouped.forEach { (expiryDate, itemsForDay) ->

            val title = getString(Res.string.notification_title)
            val body = when (itemsForDay.size) {
                1 -> getString(Res.string.notification_single_item, itemsForDay[0].name)
                2 -> getString(
                    Res.string.notification_two_items,
                    itemsForDay[0].name,
                    itemsForDay[1].name
                )

                else -> getString(
                    Res.string.notification_many_items,
                    itemsForDay[0].name,
                    itemsForDay[1].name,
                    itemsForDay.size - 2
                )
            }

            // Pass ID for Deep Linking (We use the ID of the first item as a reference)
            val deepLinkItemId = itemsForDay.first().id.hashCode()

            val dateComponents = NSDateComponents().apply {
                this.year = expiryDate.year.toLong()
                this.month = expiryDate.month.number.toLong()
                this.day = expiryDate.day.toLong()
                this.hour = time.hour.toLong()
                this.minute = time.minute.toLong()
                this.second = 0
            }

            scheduleSingle(
                title,
                body,
                dateComponents,
                expiryDate.toString(),
                deepLinkItemId
            )
        }
    }

    private fun scheduleSingle(
        title: String,
        body: String,
        dateComponents: NSDateComponents,
        idString: String,
        deepLinkId: Int
    ) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent()
        content.setTitle(title)
        content.setBody(body)
        content.setSound(UNNotificationSound.soundNamed("custom_notification_sound.wav"))

        // Pass ID so tapping opens the app (potentially to details)
        content.setUserInfo(mapOf("itemId" to deepLinkId))

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents,
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "expiry_$idString",
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("ShelfLife iOS Error: ${error.localizedDescription}")
            }
        }
    }

    actual fun cancelDaily() {
        UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
    }
}