package org.sathyasaieire.app.feature.notifications.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class AppPrefs(
    val darkMode: Boolean = false,
    val notifEvents: Boolean = true,
    val notifPolls: Boolean = true,
    val notifAnnouncements: Boolean = true,
    val analyticsEnabled: Boolean = false,
)

@Singleton
class NotificationPrefsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val messaging: FirebaseMessaging,
) {
    private val DARK_MODE = booleanPreferencesKey("dark_mode")
    private val NOTIF_EVENTS = booleanPreferencesKey("notif_events")
    private val NOTIF_POLLS = booleanPreferencesKey("notif_polls")
    private val NOTIF_ANNOUNCEMENTS = booleanPreferencesKey("notif_announcements")
    private val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")

    val prefs: Flow<AppPrefs> = dataStore.data.map { p ->
        AppPrefs(
            darkMode = p[DARK_MODE] ?: false,
            notifEvents = p[NOTIF_EVENTS] ?: true,
            notifPolls = p[NOTIF_POLLS] ?: true,
            notifAnnouncements = p[NOTIF_ANNOUNCEMENTS] ?: true,
            analyticsEnabled = p[ANALYTICS_ENABLED] ?: false,
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setNotifEvents(enabled: Boolean) {
        dataStore.edit { it[NOTIF_EVENTS] = enabled }
        runCatching {
            if (enabled) messaging.subscribeToTopic("events").await()
            else messaging.unsubscribeFromTopic("events").await()
        }
    }

    suspend fun setNotifPolls(enabled: Boolean) {
        dataStore.edit { it[NOTIF_POLLS] = enabled }
        runCatching {
            if (enabled) messaging.subscribeToTopic("polls").await()
            else messaging.unsubscribeFromTopic("polls").await()
        }
    }

    suspend fun setNotifAnnouncements(enabled: Boolean) {
        dataStore.edit { it[NOTIF_ANNOUNCEMENTS] = enabled }
        runCatching {
            if (enabled) messaging.subscribeToTopic("announcements").await()
            else messaging.unsubscribeFromTopic("announcements").await()
        }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { it[ANALYTICS_ENABLED] = enabled }
    }
}
