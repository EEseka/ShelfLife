package com.eeseka.shelflife.shared.domain.notification

import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.StringResource
import shelflife.shared.generated.resources.Res
import shelflife.shared.generated.resources.notif_1_day_after
import shelflife.shared.generated.resources.notif_3_days_after
import shelflife.shared.generated.resources.notif_3_days_before
import shelflife.shared.generated.resources.notif_month_after
import shelflife.shared.generated.resources.notif_month_before
import shelflife.shared.generated.resources.notif_today
import shelflife.shared.generated.resources.notif_tomorrow
import shelflife.shared.generated.resources.notif_week_after
import shelflife.shared.generated.resources.notif_week_before

object NotificationLogic {

    enum class NotificationType(val offsetDays: Int, val resource: StringResource) {
        // --- FUTURE (Positive Offset) ---
        MONTH_BEFORE(30, Res.string.notif_month_before),
        WEEK_BEFORE(7, Res.string.notif_week_before),
        DAYS_BEFORE_3(3, Res.string.notif_3_days_before),
        DAYS_BEFORE_1(1, Res.string.notif_tomorrow),

        // --- TODAY ---
        DAY_OF(0, Res.string.notif_today),

        // --- PAST (Negative Offset) ---
        DAY_AFTER_1(-1, Res.string.notif_1_day_after),
        DAYS_AFTER_3(-3, Res.string.notif_3_days_after),
        WEEK_AFTER(-7, Res.string.notif_week_after),
        MONTH_AFTER(-30, Res.string.notif_month_after)
    }

    /**
     * Calculates the target notification date.
     */
    fun getTriggerDate(item: PantryItem, type: NotificationType): LocalDate {
        return if (type.offsetDays >= 0) {
            item.expiryDate.minus(type.offsetDays, DateTimeUnit.DAY)
        } else {
            // Note: offsetDays is negative (e.g., -1), so we negate it to add positive days
            item.expiryDate.plus(-type.offsetDays, DateTimeUnit.DAY)
        }
    }

    /**
     * Helper for Android Receiver
     */
    fun isTriggerDay(item: PantryItem, today: LocalDate, type: NotificationType): Boolean {
        return getTriggerDate(item, type) == today
    }
}