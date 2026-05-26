package org.sathyasaieire.app.feature.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Placeholder: reschedule persistent alarms here once we store them in Room.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) return
        // Future: query stored reminders from Room and reschedule via ReminderScheduler.
    }
}
