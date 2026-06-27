package com.example.rehabilitationtraining.reminder

import java.util.Locale

data class ReminderSettings(
    val enabled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0,
) {
    init {
        require(hour in 0..23) { "hour must be between 0 and 23" }
        require(minute in 0..59) { "minute must be between 0 and 59" }
    }

    val formattedTime: String
        get() = String.format(Locale.US, "%02d:%02d", hour, minute)
}

