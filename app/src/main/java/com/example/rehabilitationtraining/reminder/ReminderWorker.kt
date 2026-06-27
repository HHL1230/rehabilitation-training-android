package com.example.rehabilitationtraining.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val settings = ReminderSettingsStore(applicationContext).getSettings()
        if (!settings.enabled) return Result.success()

        NotificationHelper.showTrainingReminder(applicationContext)
        ReminderScheduler(applicationContext).rescheduleAfterCurrentRun(settings)
        return Result.success()
    }
}
