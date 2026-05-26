package org.sathyasaieire.app.feature.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.sathyasaieire.app.MainActivity
import org.sathyasaieire.app.R
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject lateinit var firestore: FirebaseFirestore
    @Inject lateinit var auth: FirebaseAuth

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onNewToken(token: String) {
        // Subscribe to default topics on fresh registration
        with(FirebaseMessaging.getInstance()) {
            subscribeToTopic(CHANNEL_EVENTS)
            subscribeToTopic(CHANNEL_ANNOUNCEMENTS)
            subscribeToTopic(CHANNEL_POLLS)
        }
        // Persist token to user doc (best-effort)
        val uid = auth.currentUser?.uid ?: return
        scope.launch {
            runCatching {
                firestore.collection("users").document(uid)
                    .update("fcmToken", token).await()
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val channelId = message.data["channel"] ?: CHANNEL_GENERAL
        showNotification(title, body, channelId)
    }

    private fun showNotification(title: String, body: String, channelId: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannels(nm)

        val tapIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_splash_om)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(tapIntent)
            .build()

        nm.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun ensureChannels(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        listOf(
            Triple(CHANNEL_EVENTS, "Event Updates", NotificationManager.IMPORTANCE_DEFAULT),
            Triple(CHANNEL_POLLS, "Polls", NotificationManager.IMPORTANCE_DEFAULT),
            Triple(CHANNEL_ANNOUNCEMENTS, "Announcements", NotificationManager.IMPORTANCE_HIGH),
            Triple(CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_DEFAULT),
        ).forEach { (id, name, importance) ->
            nm.createNotificationChannel(NotificationChannel(id, name, importance))
        }
    }

    companion object {
        const val CHANNEL_EVENTS = "events"
        const val CHANNEL_POLLS = "polls"
        const val CHANNEL_ANNOUNCEMENTS = "announcements"
        const val CHANNEL_GENERAL = "general"
    }
}
