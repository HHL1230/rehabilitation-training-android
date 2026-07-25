package com.example.rehabilitationtraining.ui

import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType
import com.example.rehabilitationtraining.domain.TrainingStats
import com.example.rehabilitationtraining.reminder.ReminderSettings
import java.time.LocalDate
import java.time.LocalTime

data class ReminderTimeInput(
    val hour: String = "9",
    val minute: String = "0",
)

data class TrainingUiState(
    val selectedDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val selectedType: TrainingType = TrainingType.BAND_LEG_CURL,
    val recordHour: String = LocalTime.now().hour.toString(),
    val recordMinute: String = LocalTime.now().minute.toString(),
    val durationMinutes: String = "",
    val sets: String = "",
    val reps: String = "",
    val weightKg: String = "",
    val resistanceLevel: String = "",
    val distanceKm: String = "",
    val notes: String = "",
    val records: List<TrainingRecordEntity> = emptyList(),
    val stats: TrainingStats = TrainingStats.Empty,
    val reminderSettingsByType: Map<TrainingType, ReminderSettings> =
        TrainingType.entries.associateWith { ReminderSettings() },
    val reminderTimeInputsByType: Map<TrainingType, ReminderTimeInput> =
        TrainingType.entries.associateWith { ReminderTimeInput() },
    val validationMessages: List<String> = emptyList(),
    val statusMessage: String? = null,
)
