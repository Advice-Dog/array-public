package com.advice.array.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.advice.array.MainActivity
import com.advice.array.R
import com.advice.array.api.UnraidRepository
import com.advice.array.models.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber


object NotificationHelper {

    private const val CHANNEL_ID = "notification_channel"
    const val NOTIFICATION_DEEP_LINK = "deep_link"

    fun clearNotifications(context: Context, expect: List<Int> = emptyList()) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.activeNotifications.forEach {
                if (it.id !in expect) {
                    Timber.e(it.id.toString())
                    notificationManager.cancel(it.id)
                }
            }
        }
    }

    fun postNotification(context: Context, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = context.getString(R.string.status_channel_name)
            val descriptionText = context.getString(R.string.status_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        val onCancelIntent = Intent(context, OnCancelBroadcastReceiver::class.java)
        onCancelIntent.putExtra("id", notification.file)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val onDismissPendingIntent =
            PendingIntent.getBroadcast(context, 0, onCancelIntent, flag)

        val resultIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(NOTIFICATION_DEEP_LINK, true)
        }
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.unraid_red))
            .setContentTitle(notification.subject)
            .setContentText(notification.description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)
            .setDeleteIntent(onDismissPendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(notification.hashCode(), builder.build())
        }
    }

    class OnCancelBroadcastReceiver : BroadcastReceiver(), KoinComponent {

        private val repository by inject<UnraidRepository>()

        private val scope = CoroutineScope(Dispatchers.IO)

        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getStringExtra("id") ?: ""
            scope.launch {
                repository.dismissNotification(id)
            }
        }
    }
}