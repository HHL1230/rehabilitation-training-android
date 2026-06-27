package com.example.rehabilitationtraining.reminder

import android.content.Context

class ReminderSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "training_reminder_settings",
        Context.MODE_PRIVATE,
    )

    fun getSettings(): ReminderSettings = ReminderSettings(
        enabled = preferences.getBoolean(KEY_ENABLED, false),
        hour = preferences.getInt(KEY_HOUR, 9),
        minute = preferences.getInt(KEY_MINUTE, 0),
    )

    fun save(settings: ReminderSettings) {
        preferences.edit()
            .putBoolean(KEY_ENABLED, settings.enabled)
            .putInt(KEY_HOUR, settings.hour)
            .putInt(KEY_MINUTE, settings.minute)
            .apply()
    }

    private companion object {
        const val KEY_ENABLED = "enabled"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"
    }
}

