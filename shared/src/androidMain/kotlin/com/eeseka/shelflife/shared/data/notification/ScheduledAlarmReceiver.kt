package com.eeseka.shelflife.shared.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.eeseka.shelflife.shared.R
import com.eeseka.shelflife.shared.domain.database.local.LocalPantryStorageService
import com.eeseka.shelflife.shared.domain.notification.NotificationLogic
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import shelflife.shared.generated.resources.Res
import shelflife.shared.generated.resources.item_group_many
import shelflife.shared.generated.resources.item_group_three
import shelflife.shared.generated.resources.item_group_two
import shelflife.shared.generated.resources.notification_title
import java.net.URL
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ScheduledAlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val localStorage: LocalPantryStorageService by inject()

    companion object {
        private const val CHANNEL_ID = "expiry_alerts"
    }

    @OptIn(ExperimentalTime::class)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val allItems = localStorage.getAllPantryItems().first()

                NotificationLogic.NotificationType.entries.forEach { type ->
                    val matchingItems = allItems.filter { item ->
                        NotificationLogic.isTriggerDay(item, today, type)
                    }

                    if (matchingItems.isNotEmpty()) {
                        processNotificationForType(context, matchingItems, type)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun processNotificationForType(
        context: Context,
        items: List<PantryItem>,
        type: NotificationLogic.NotificationType
    ) {
        val itemString = when (items.size) {
            1 -> items[0].name
            2 -> getString(Res.string.item_group_two, items[0].name, items[1].name)
            3 -> getString(Res.string.item_group_three, items[0].name, items[1].name, items[2].name)
            else -> getString(
                Res.string.item_group_many,
                items[0].name,
                items[1].name,
                items.size - 2
            )
        }

        val title = getString(Res.string.notification_title)
        val body = getString(type.resource, itemString)
        val imageUrl = if (items.size == 1) items[0].imageUrl else null

        // System ID (Int) for Notification Manager
        val notificationId = 1000 + type.ordinal
        // Request Code (Int) for PendingIntent uniqueness
        val requestCode = items[0].id.hashCode()
        // Navigation ID (String) for the App to use
        val itemId = items[0].id

        showNotification(context, notificationId, requestCode, itemId, title, body, imageUrl)
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        requestCode: Int,
        itemId: String,
        title: String,
        body: String,
        imageUrl: String?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        runBlocking { createChannel(context) }

        val notificationManager = NotificationManagerCompat.from(context)

        val launchIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("itemId", itemId)
            }

        val contentIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context,
                requestCode,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        var largeIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.app_logo_foreground)
        } catch (_: Exception) {
            null
        }

        if (imageUrl != null) {
            try {
                val url = URL(imageUrl)
                val downloadedBitmap =
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                if (downloadedBitmap != null) largeIconBitmap = downloadedBitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        if (largeIconBitmap != null) {
            builder.setLargeIcon(largeIconBitmap)
            if (imageUrl != null) {
                builder.setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(largeIconBitmap)
                        .setSummaryText(body)
                )
            }
        }

        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun createChannel(context: Context) {
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_description)
        val soundUri =
            "android.resource://${context.packageName}/raw/custom_notification_sound".toUri()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channel =
            NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = descriptionText
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}