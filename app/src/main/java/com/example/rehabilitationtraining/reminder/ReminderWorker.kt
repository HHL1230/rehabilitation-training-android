package com.example.rehabilitationtraining.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rehabilitationtraining.data.TrainingType

class ReminderWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val typeName = inputData.getString(KEY_REMINDER_TRAINING_TYPE)
        if (typeName == null) {
            val settingsByType = ReminderSettingsStore(applicationContext).getAllSettings()
            ReminderScheduler(applicationContext).applyAllSettings(settingsByType)
            return Result.success()
        }

        val type = TrainingType.entries.firstOrNull { it.name == typeName } ?: return Result.failure()
        val settings = ReminderSettingsStore(applicationContext).getSettings(type)
        if (!settings.enabled) return Result.success()

        NotificationHelper.showTrainingReminder(applicationContext, type)
        ReminderScheduler(applicationContext).rescheduleAfterCurrentRun(type, settings)
        return Result.success()
    }
}
