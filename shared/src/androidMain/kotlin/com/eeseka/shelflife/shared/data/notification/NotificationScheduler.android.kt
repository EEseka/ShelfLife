package com.eeseka.shelflife.shared.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import kotlinx.datetime.LocalTime
import java.util.Calendar

actual class NotificationScheduler(
    private val context: Context,
    private val logger: ShelfLifeLogger
) {
    companion object {
        private const val DAILY_ALARM_ID = 1001
    }

    // items list is IGNORED here. The Receiver fetches fresh data.
    actual suspend fun scheduleDaily(time: LocalTime, items: List<PantryItem>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DATE, 1)
        }

        val intent = Intent(context, ScheduledAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_ALARM_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, target.timeInMillis, pendingIntent)
                }
            } else {
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.RTC_WAKEUP,
                    target.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            logger.error("NotificationScheduler Error (SecurityException)", e)
        }
    }

    actual fun cancelDaily() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ScheduledAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_ALARM_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}