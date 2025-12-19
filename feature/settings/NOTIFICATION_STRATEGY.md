# Notification Strategy - Industry Standard Approach

## Overview

This document explains the proper approach for implementing notifications in ShelfLife, following industry best practices for battery efficiency and user experience.

## The Strategy: One Daily Alarm

### ❌ What NOT to Do
**Don't create individual alarms for every single item:**
```kotlin
// BAD - Creates hundreds of alarms
items.forEach { item ->
    scheduleAlarm(item.expiryDate)  // One alarm per item
}
```

**Why this is bad:**
- Battery drain (system wakes device for each alarm)
- System alarm limit (Android limits ~500 alarms per app)
- Inefficient memory usage
- Difficult to manage and cancel

### ✅ The Correct Approach
**Create ONE repeating daily alarm at the user's chosen time:**

```kotlin
// GOOD - One daily alarm
notificationScheduler.scheduleDailyAlarm(
    time = LocalTime(hour = 8, minute = 0)  // User's chosen time
)
```

**When the alarm fires:**
1. Query database for items expiring soon (e.g., within 3 days)
2. Group items by urgency:
   - Expired today
   - Expiring tomorrow
   - Expiring in 2-3 days
3. Show notification(s) based on what's found

## Implementation Architecture

### Components Needed

#### 1. NotificationScheduler (Platform-Specific)

```kotlin
// commonMain
expect class NotificationScheduler {
    fun scheduleDailyAlarm(time: LocalTime)
    fun cancelDailyAlarm()
    fun isAlarmScheduled(): Boolean
}

// androidMain
actual class NotificationScheduler(
    private val context: Context,
    private val alarmManager: AlarmManager
) {
    actual fun scheduleDailyAlarm(time: LocalTime) {
        // Use AlarmManager.setRepeating() or setInexactRepeating()
        // For Android 12+, use AlarmManager.setExactAndAllowWhileIdle()
    }
    
    actual fun cancelDailyAlarm() {
        // Cancel the pending intent
    }
    
    actual fun isAlarmScheduled(): Boolean {
        // Check if alarm exists
    }
}

// iosMain
actual class NotificationScheduler {
    actual fun scheduleDailyAlarm(time: LocalTime) {
        // Use UNNotificationCenter
        // Create UNCalendarNotificationTrigger with daily repeat
    }
    
    actual fun cancelDailyAlarm() {
        // Remove scheduled notification
    }
    
    actual fun isAlarmScheduled(): Boolean {
        // Check pending notifications
    }
}
```

#### 2. NotificationWorker / Handler

**Android - WorkManager:**
```kotlin
class ExpiryCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // 1. Query items expiring soon
        val expiringItems = repository.getItemsExpiringSoon(days = 3)
        
        if (expiringItems.isNotEmpty()) {
            // 2. Group by urgency
            val expiredToday = expiringItems.filter { it.isExpiredToday() }
            val expiringTomorrow = expiringItems.filter { it.expiringTomorrow() }
            
            // 3. Show notification
            notificationManager.showExpiryNotification(
                expiredCount = expiredToday.size,
                expiringCount = expiringTomorrow.size
            )
        }
        
        return Result.success()
    }
}
```

**iOS - NotificationHandler:**
```kotlin
// Called when notification trigger fires
class NotificationHandler {
    suspend fun handleDailyCheck() {
        val expiringItems = repository.getItemsExpiringSoon(days = 3)
        
        if (expiringItems.isNotEmpty()) {
            showLocalNotification(
                title = "Items Expiring Soon",
                body = "${expiringItems.size} items need your attention"
            )
        }
    }
}
```

#### 3. NotificationManager (Show Notifications)

```kotlin
expect class NotificationManager {
    fun showExpiryNotification(
        expiredCount: Int,
        expiringCount: Int
    )
    
    fun cancelAll()
}
```

## Integration with SettingsViewModel

### When User Enables Notifications:

```kotlin
private fun handlePermissionResult(permissionState: PermissionState) {
    viewModelScope.launch {
        when (permissionState) {
            PermissionState.GRANTED -> {
                // 1. Save preference
                settingsService.setNotificationPreferences(
                    state.value.notification.copy(allowed = true)
                )
                
                // 2. Schedule daily alarm
                notificationScheduler.scheduleDailyAlarm(
                    time = state.value.notification.reminderTime
                )
            }
            // ... other cases
        }
    }
}
```

### When User Changes Notification Time:

```kotlin
private fun changeNotificationTime(time: LocalTime) {
    viewModelScope.launch {
        // 1. Update preference
        settingsService.setNotificationPreferences(
            state.value.notification.copy(reminderTime = time)
        )
        
        // 2. Reschedule alarm if notifications are enabled
        if (state.value.notification.allowed) {
            notificationScheduler.cancelDailyAlarm()
            notificationScheduler.scheduleDailyAlarm(time)
        }
    }
}
```

### When User Disables Notifications:

```kotlin
private fun toggleNotifications() {
    if (currentPreference) {
        viewModelScope.launch {
            // 1. Update preference
            settingsService.setNotificationPreferences(
                state.value.notification.copy(allowed = false)
            )
            
            // 2. Cancel daily alarm
            notificationScheduler.cancelDailyAlarm()
        }
    }
}
```

## Platform-Specific Details

### Android
- **AlarmManager** for scheduling daily alarm
- **WorkManager** for executing the check (more reliable than BroadcastReceiver)
- **NotificationCompat** for showing notifications
- **Notification channels** required for Android 8.0+

### iOS
- **UNUserNotificationCenter** for scheduling and permissions
- **UNCalendarNotificationTrigger** for daily repeat
- **App extension** or content provider for querying data when notification fires

## Example Notification Content

### Single Notification with Summary:

```
Title: Items Expiring Soon
Body: 3 items expired today, 5 expiring tomorrow

Actions:
- View Items (opens app)
- Dismiss
```

### Expanded Notification (Android):

```
Title: Items Expiring Soon
Body:
  🔴 Expired: Milk, Eggs, Yogurt
  ⚠️ Tomorrow: Chicken, Cheese, Lettuce
  
Actions:
- View All (opens app)
- Dismiss
```

## Best Practices

1. **One alarm per app** - Don't create multiple alarms
2. **Batch notifications** - Group multiple items in one notification
3. **Respect user time** - Only notify at the chosen time
4. **Battery efficient** - Use inexact alarms when possible (Android)
5. **Graceful degradation** - Handle permission denials gracefully
6. **Persistence** - Reschedule alarms after device reboot

## Testing Checklist

- [ ] Alarm schedules correctly at chosen time
- [ ] Notification shows when items are expiring
- [ ] Alarm cancels when notifications disabled
- [ ] Alarm reschedules when time changes
- [ ] Alarm persists after app restart
- [ ] Alarm reschedules after device reboot
- [ ] Works on Android (multiple versions)
- [ ] Works on iOS (multiple versions)

## Future Enhancements

1. **Smart notifications** - Adjust frequency based on user interaction
2. **Categories** - Notify for specific categories only
3. **Urgency levels** - Different notifications for different urgency
4. **Snooze functionality** - Allow users to snooze notifications
5. **Quick actions** - Mark as consumed from notification

---

## Summary

**One daily alarm at user's chosen time** is the industry standard approach because it:
- Saves battery life
- Respects system limits
- Provides better UX
- Easier to implement and maintain

When the alarm fires, check database for expiring items and show appropriate notification(s).
