package com.example.rehabilitationtraining.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

const val UNIQUE_REMINDER_WORK_NAME = "daily_training_reminder"

class ReminderScheduler(private val context: Context) {
    fun applySettings(settings: ReminderSettings) {
        if (settings.enabled) {
            scheduleDaily(settings)
        } else {
            cancel()
        }
    }

    fun scheduleDaily(settings: ReminderSettings) {
        enqueueDaily(settings, ExistingWorkPolicy.REPLACE)
    }

    fun rescheduleAfterCurrentRun(settings: ReminderSettings) {
        enqueueDaily(settings, ExistingWorkPolicy.APPEND_OR_REPLACE)
    }

    private fun enqueueDaily(settings: ReminderSettings, policy: ExistingWorkPolicy) {
        require(settings.enabled) { "settings must be enabled before scheduling reminders" }
        val delayMillis = nextDelayMillis(settings.hour, settings.minute)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            UNIQUE_REMINDER_WORK_NAME,
            policy,
            request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_REMINDER_WORK_NAME)
    }

    companion object {
        fun nextDelayMillis(
            hour: Int,
            minute: Int,
            now: LocalDateTime = LocalDateTime.now(),
        ): Long {
            val next = now
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0)
                .let { candidate ->
                    if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
                }

            return Duration.between(now, next).toMillis().coerceAtLeast(1L)
        }
    }
}
