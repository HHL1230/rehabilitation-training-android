package com.example.rehabilitationtraining

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.rehabilitationtraining.data.TrainingDatabase
import com.example.rehabilitationtraining.data.TrainingRepository
import com.example.rehabilitationtraining.reminder.ReminderScheduler
import com.example.rehabilitationtraining.reminder.ReminderSettingsStore
import com.example.rehabilitationtraining.ui.RehabilitationTrainingApp
import com.example.rehabilitationtraining.ui.TrainingViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = TrainingDatabase.getInstance(applicationContext)
        val repository = TrainingRepository(database.trainingRecordDao())
        val reminderSettingsStore = ReminderSettingsStore(applicationContext)
        val reminderScheduler = ReminderScheduler(applicationContext)
        val viewModelFactory = TrainingViewModelFactory(
            repository = repository,
            reminderSettingsStore = reminderSettingsStore,
            reminderScheduler = reminderScheduler,
        )

        setContent {
            RehabilitationTrainingApp(viewModelFactory)
        }
    }
}
