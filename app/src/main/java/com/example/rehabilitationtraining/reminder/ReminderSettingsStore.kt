package com.example.rehabilitationtraining.reminder

import android.content.Context
import com.example.rehabilitationtraining.data.TrainingType

class ReminderSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "training_reminder_settings",
        Context.MODE_PRIVATE,
    )

    fun getAllSettings(): Map<TrainingType, ReminderSettings> =
        TrainingType.entries.associateWith { getSettings(it) }

    fun getSettings(type: TrainingType): ReminderSettings {
        val defaults = defaultSettings(type)
        return ReminderSettings(
            enabled = preferences.getBoolean(enabledKey(type), legacyEnabled(type) ?: defaults.enabled),
            hour = preferences.getInt(hourKey(type), legacyHour(type) ?: defaults.hour),
            minute = preferences.getInt(minuteKey(type), legacyMinute(type) ?: defaults.minute),
        )
    }

    fun save(type: TrainingType, settings: ReminderSettings) {
        preferences.edit()
            .putBoolean(enabledKey(type), settings.enabled)
            .putInt(hourKey(type), settings.hour)
            .putInt(minuteKey(type), settings.minute)
            .apply()
    }

    private companion object {
        const val KEY_ENABLED = "enabled"
        const val KEY_HOUR = "hour"
        const val KEY_MINUTE = "minute"

        fun defaultSettings(type: TrainingType): ReminderSettings =
            when (type) {
                TrainingType.BAND_LEG_CURL -> ReminderSettings(enabled = false, hour = 9, minute = 0)
                TrainingType.LEG_EXTENSION -> ReminderSettings(enabled = false, hour = 14, minute = 0)
                TrainingType.RESISTED_CYCLING -> ReminderSettings(enabled = false, hour = 18, minute = 0)
            }

        fun enabledKey(type: TrainingType): String = "enabled_${type.name}"

        fun hourKey(type: TrainingType): String = "hour_${type.name}"

        fun minuteKey(type: TrainingType): String = "minute_${type.name}"
    }

    private fun legacyEnabled(type: TrainingType): Boolean? =
        if (type == TrainingType.BAND_LEG_CURL && preferences.contains(KEY_ENABLED)) {
            preferences.getBoolean(KEY_ENABLED, false)
        } else {
            null
        }

    private fun legacyHour(type: TrainingType): Int? =
        if (type == TrainingType.BAND_LEG_CURL && preferences.contains(KEY_HOUR)) {
            preferences.getInt(KEY_HOUR, 9)
        } else {
            null
        }

    private fun legacyMinute(type: TrainingType): Int? =
        if (type == TrainingType.BAND_LEG_CURL && preferences.contains(KEY_MINUTE)) {
            preferences.getInt(KEY_MINUTE, 0)
        } else {
            null
        }
}
