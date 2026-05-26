package org.sathyasaieire.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SaiApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Analytics and Crashlytics are opt-in (GDPR requirement for Ireland).
        // They are only enabled after the user grants consent on the GdprConsentScreen.
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
    }
}
