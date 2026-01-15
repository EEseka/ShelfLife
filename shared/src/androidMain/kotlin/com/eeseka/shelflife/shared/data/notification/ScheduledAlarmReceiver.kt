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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import shelflife.shared.generated.resources.Res
import shelflife.shared.generated.resources.notification_many_items
import shelflife.shared.generated.resources.notification_single_item
import shelflife.shared.generated.resources.notification_title
import shelflife.shared.generated.resources.notification_two_items
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
                //  QUERY
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val allItems = localStorage.getAllPantryItems().first()

                // Filter items expiring today (or passed)
                val expiringItems = allItems.filter { it.expiryDate <= today }

                if (expiringItems.isNotEmpty()) {

                    // PREPARE CONTENT (Using KMP Resources)
                    val title = getString(Res.string.notification_title)
                    val body = when (expiringItems.size) {
                        1 -> getString(Res.string.notification_single_item, expiringItems[0].name)
                        2 -> getString(
                            Res.string.notification_two_items,
                            expiringItems[0].name,
                            expiringItems[1].name
                        )

                        else -> getString(
                            Res.string.notification_many_items,
                            expiringItems[0].name,
                            expiringItems[1].name,
                            expiringItems.size - 2
                        )
                    }

                    // 3. IMAGE LOGIC
                    // If exactly 1 item, try to get its image, Otherwise null.
                    val imageUrl = if (expiringItems.size == 1) expiringItems[0].imageUrl else null
                    val itemId = expiringItems[0].id.hashCode() // Deep link to the first item

                    showNotification(context, itemId, title, body, imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        id: Int,
        title: String,
        body: String,
        imageUrl: String?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val notificationManager = NotificationManagerCompat.from(context)
        // We need to call a suspend function for channel name, so do it inside coroutine scope
        // For simplicity here, calling a helper that runs blocking or suspend wrapper
        createChannel(context)

        val launchIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("itemId", id) // Pass ID for navigation
            }

        val contentIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                context,
                id,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        // Default Large Icon (App Logo)
        var largeIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.app_logo_foreground)
        } catch (_: Exception) {
            null
        }

        // --- DYNAMIC IMAGE DOWNLOADING ---
        if (imageUrl != null) {
            try {
                val url = URL(imageUrl)
                val downloadedBitmap =
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                if (downloadedBitmap != null) {
                    largeIconBitmap = downloadedBitmap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // Expandable text
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)

        if (largeIconBitmap != null) {
            builder.setLargeIcon(largeIconBitmap)
            // Optional: If you want the Big Picture style for single items
            if (imageUrl != null) {
                builder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(largeIconBitmap)
                        .setSummaryText(body)
                )
            }
        }

        try {
            notificationManager.notify(id, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun createChannel(context: Context) {
        val soundUri =
            "android.resource://${context.packageName}/raw/custom_notification_sound".toUri()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_description)

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