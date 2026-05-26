package org.sathyasaieire.app.feature.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import org.sathyasaieire.app.domain.model.Event
import javax.inject.Inject
import javax.inject.Singleton

enum class ReminderOffset(val label: String, val millis: Long) {
    MIN_10("10 minutes before", 10 * 60_000L),
    HOUR_1("1 hour before", 60 * 60_000L),
    DAY_1("1 day before", 24 * 60 * 60_000L),
}

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(event: Event, offset: ReminderOffset) {
        val triggerAt = event.dateTimeMs - offset.millis
        if (triggerAt <= System.currentTimeMillis()) return // already past

        val pendingIntent = buildPendingIntent(event, offset)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Fall back to inexact alarm if exact permission not granted
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            return
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    fun cancel(event: Event, offset: ReminderOffset) {
        alarmManager.cancel(buildPendingIntent(event, offset))
    }

    private fun buildPendingIntent(event: Event, offset: ReminderOffset): PendingIntent {
        val intent = Intent(context, EventAlarmReceiver::class.java).apply {
            putExtra(EventAlarmReceiver.EXTRA_EVENT_ID, event.id)
            putExtra(EventAlarmReceiver.EXTRA_EVENT_TITLE, event.title)
            putExtra(EventAlarmReceiver.EXTRA_EVENT_TIME, event.formatTime())
        }
        val requestCode = (event.id + offset.name).hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
