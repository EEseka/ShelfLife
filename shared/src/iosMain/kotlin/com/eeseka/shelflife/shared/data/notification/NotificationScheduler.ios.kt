package com.eeseka.shelflife.shared.data.notification

import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.notification.NotificationLogic
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import shelflife.shared.generated.resources.Res
import shelflife.shared.generated.resources.item_group_many
import shelflife.shared.generated.resources.item_group_three
import shelflife.shared.generated.resources.item_group_two
import shelflife.shared.generated.resources.notification_title
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

actual class NotificationScheduler(
    private val logger: ShelfLifeLogger
) {

    @OptIn(ExperimentalTime::class)
    actual suspend fun scheduleDaily(time: LocalTime, items: List<PantryItem>) {
        cancelDaily()

        val center = UNUserNotificationCenter.currentNotificationCenter()

        // Use explicitly imported Clock to avoid ambiguity
        val nowInstant = Clock.System.now()
        val now = nowInstant.toLocalDateTime(TimeZone.currentSystemDefault())

        val allEvents = items.flatMap { item ->
            NotificationLogic.NotificationType.entries.map { type ->
                val triggerDate = NotificationLogic.getTriggerDate(item, type)
                Triple(item, type, triggerDate)
            }
        }

        val validEvents = allEvents.filter { (_, _, date) ->
            val triggerDateTime = LocalDateTime(date, time)
            triggerDateTime > now
        }

        val sortedEvents = validEvents
            .sortedBy { it.third }
            .take(64)

        val groupedEvents = sortedEvents.groupBy { it.third to it.second }

        groupedEvents.forEach { (key, eventList) ->
            val (fireDate, type) = key
            val itemsInGroup = eventList.map { it.first }

            val itemString = when (itemsInGroup.size) {
                1 -> itemsInGroup[0].name
                2 -> getString(
                    Res.string.item_group_two,
                    itemsInGroup[0].name,
                    itemsInGroup[1].name
                )

                3 -> getString(
                    Res.string.item_group_three,
                    itemsInGroup[0].name,
                    itemsInGroup[1].name,
                    itemsInGroup[2].name
                )

                else -> getString(
                    Res.string.item_group_many,
                    itemsInGroup[0].name,
                    itemsInGroup[1].name,
                    itemsInGroup.size - 2
                )
            }

            val title = getString(Res.string.notification_title)
            val body = getString(type.resource, itemString)

            val components = NSDateComponents().apply {
                this.year = fireDate.year.toLong()
                this.month = fireDate.month.number.toLong()
                this.day = fireDate.day.toLong()
                this.hour = time.hour.toLong()
                this.minute = time.minute.toLong()
                this.second = 0
            }

            val content = UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(body)
                setSound(UNNotificationSound.soundNamed("custom_notification_sound.wav"))
                setUserInfo(mapOf("itemId" to itemsInGroup.first().id))
            }

            val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                dateComponents = components,
                repeats = false
            )

            val requestId = "${fireDate}_${type.name}"
            val request = UNNotificationRequest.requestWithIdentifier(requestId, content, trigger)

            center.addNotificationRequest(request) { error ->
                if (error != null) logger.error("ShelfLife iOS Error: ${error.localizedDescription}")
            }
        }
    }

    actual fun cancelDaily() {
        UNUserNotificationCenter.currentNotificationCenter().removeAllPendingNotificationRequests()
    }
}