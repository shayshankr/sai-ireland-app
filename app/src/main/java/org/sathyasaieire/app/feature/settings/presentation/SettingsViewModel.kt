package org.sathyasaieire.app.feature.settings.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.sathyasaieire.app.feature.notifications.data.AppPrefs
import org.sathyasaieire.app.feature.notifications.data.NotificationPrefsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: NotificationPrefsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val prefs: StateFlow<AppPrefs> = prefsRepo.prefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppPrefs())

    fun toggleDarkMode() = viewModelScope.launch {
        prefsRepo.setDarkMode(!prefs.value.darkMode)
    }

    fun toggleNotifEvents() = viewModelScope.launch {
        prefsRepo.setNotifEvents(!prefs.value.notifEvents)
    }

    fun toggleNotifPolls() = viewModelScope.launch {
        prefsRepo.setNotifPolls(!prefs.value.notifPolls)
    }

    fun toggleNotifAnnouncements() = viewModelScope.launch {
        prefsRepo.setNotifAnnouncements(!prefs.value.notifAnnouncements)
    }

    fun toggleAnalytics() = viewModelScope.launch {
        val newValue = !prefs.value.analyticsEnabled
        prefsRepo.setAnalyticsEnabled(newValue)
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(newValue)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = newValue
    }
}
