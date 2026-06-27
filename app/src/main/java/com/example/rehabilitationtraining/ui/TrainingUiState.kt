package com.example.rehabilitationtraining.ui

import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType
import com.example.rehabilitationtraining.domain.TrainingStats
import com.example.rehabilitationtraining.reminder.ReminderSettings
import java.time.LocalDate

data class TrainingUiState(
    val selectedDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val selectedType: TrainingType = TrainingType.BAND_LEG_CURL,
    val durationMinutes: String = "",
    val sets: String = "",
    val reps: String = "",
    val weightKg: String = "",
    val resistanceLevel: String = "",
    val notes: String = "",
    val records: List<TrainingRecordEntity> = emptyList(),
    val stats: TrainingStats = TrainingStats.Empty,
    val reminderSettings: ReminderSettings = ReminderSettings(),
    val reminderHour: String = "9",
    val reminderMinute: String = "0",
    val validationMessages: List<String> = emptyList(),
    val statusMessage: String? = null,
)

