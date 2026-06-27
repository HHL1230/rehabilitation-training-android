package com.example.rehabilitationtraining.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val settings = ReminderSettingsStore(context).getSettings()
        if (settings.enabled) {
            ReminderScheduler(context).scheduleDaily(settings)
        }
    }
}

