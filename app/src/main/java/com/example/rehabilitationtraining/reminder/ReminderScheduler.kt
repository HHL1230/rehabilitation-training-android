package com.example.rehabilitationtraining.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.rehabilitationtraining.data.TrainingType
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

const val UNIQUE_REMINDER_WORK_NAME = "daily_training_reminder"
const val KEY_REMINDER_TRAINING_TYPE = "training_type"

class ReminderScheduler(private val context: Context) {
    fun applyAllSettings(settingsByType: Map<TrainingType, ReminderSettings>) {
        TrainingType.entries.forEach { type ->
            applySettings(type, settingsByType.getValue(type))
        }
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_REMINDER_WORK_NAME)
    }

    fun applySettings(type: TrainingType, settings: ReminderSettings) {
        if (settings.enabled) {
            scheduleDaily(type, settings)
        } else {
            cancel(type)
        }
    }

    fun scheduleDaily(type: TrainingType, settings: ReminderSettings) {
        enqueueDaily(type, settings, ExistingWorkPolicy.REPLACE)
    }

    fun rescheduleAfterCurrentRun(type: TrainingType, settings: ReminderSettings) {
        enqueueDaily(type, settings, ExistingWorkPolicy.APPEND_OR_REPLACE)
    }

    private fun enqueueDaily(
        type: TrainingType,
        settings: ReminderSettings,
        policy: ExistingWorkPolicy,
    ) {
        require(settings.enabled) { "settings must be enabled before scheduling reminders" }
        val delayMillis = nextDelayMillis(settings.hour, settings.minute)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_REMINDER_TRAINING_TYPE to type.name))
            .build()

        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            uniqueWorkName(type),
            policy,
            request,
        )
    }

    fun cancel(type: TrainingType) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(uniqueWorkName(type))
    }

    private fun uniqueWorkName(type: TrainingType): String =
        "${UNIQUE_REMINDER_WORK_NAME}_${type.name}"

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
