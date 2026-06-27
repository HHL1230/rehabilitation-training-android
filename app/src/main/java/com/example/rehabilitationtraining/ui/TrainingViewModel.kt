package com.example.rehabilitationtraining.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rehabilitationtraining.data.TrainingRepository
import com.example.rehabilitationtraining.data.TrainingType
import com.example.rehabilitationtraining.domain.TrainingRecordDraft
import com.example.rehabilitationtraining.domain.TrainingStats
import com.example.rehabilitationtraining.domain.validate
import com.example.rehabilitationtraining.reminder.ReminderScheduler
import com.example.rehabilitationtraining.reminder.ReminderSettings
import com.example.rehabilitationtraining.reminder.ReminderSettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class TrainingViewModel(
    private val repository: TrainingRepository,
    private val reminderSettingsStore: ReminderSettingsStore,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    init {
        val reminderSettings = reminderSettingsStore.getSettings()
        _uiState.update {
            it.copy(
                reminderSettings = reminderSettings,
                reminderHour = reminderSettings.hour.toString(),
                reminderMinute = reminderSettings.minute.toString(),
            )
        }

        viewModelScope.launch {
            repository.observeAll().collect { records ->
                _uiState.update {
                    it.copy(
                        records = records,
                        stats = TrainingStats.fromRecords(records, LocalDate.now().toEpochDay()),
                    )
                }
            }
        }
    }

    fun selectType(type: TrainingType) {
        _uiState.update { it.copy(selectedType = type, validationMessages = emptyList()) }
    }

    fun setSelectedDate(offsetFromToday: Long) {
        _uiState.update {
            it.copy(
                selectedDateEpochDay = LocalDate.now().plusDays(offsetFromToday).toEpochDay(),
                validationMessages = emptyList(),
            )
        }
    }

    fun updateDurationMinutes(value: String) {
        updateTextField(value) { copy(durationMinutes = it) }
    }

    fun updateSets(value: String) {
        updateTextField(value) { copy(sets = it) }
    }

    fun updateReps(value: String) {
        updateTextField(value) { copy(reps = it) }
    }

    fun updateWeightKg(value: String) {
        updateTextField(value) { copy(weightKg = it) }
    }

    fun updateResistanceLevel(value: String) {
        updateTextField(value) { copy(resistanceLevel = it) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value.take(200), validationMessages = emptyList()) }
    }

    fun saveRecord() {
        val draft = _uiState.value.toDraft()
        val errors = draft.validate()
        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    validationMessages = errors.map { error -> error.message },
                    statusMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            repository.addDraft(draft)
            _uiState.update {
                it.copy(
                    durationMinutes = "",
                    sets = "",
                    reps = "",
                    weightKg = "",
                    resistanceLevel = "",
                    notes = "",
                    validationMessages = emptyList(),
                    statusMessage = "已儲存訓練紀錄",
                )
            }
        }
    }

    fun updateReminderHour(value: String) {
        updateTextField(value.take(2)) { copy(reminderHour = it) }
    }

    fun updateReminderMinute(value: String) {
        updateTextField(value.take(2)) { copy(reminderMinute = it) }
    }

    fun setReminderEnabled(enabled: Boolean) {
        saveReminderSettings(enabledOverride = enabled)
    }

    fun saveReminderTime() {
        saveReminderSettings(enabledOverride = null)
    }

    fun recordsFromLast30Days(): List<com.example.rehabilitationtraining.data.TrainingRecordEntity> {
        val today = LocalDate.now().toEpochDay()
        return _uiState.value.records.filter { it.dateEpochDay in (today - 29)..today }
    }

    fun showStatus(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    private fun updateTextField(value: String, update: TrainingUiState.(String) -> TrainingUiState) {
        _uiState.update { state ->
            if (value.all { it.isDigit() || it == '.' }) {
                state.update(value).copy(validationMessages = emptyList())
            } else {
                state
            }
        }
    }

    private fun saveReminderSettings(enabledOverride: Boolean?) {
        val state = _uiState.value
        val hour = state.reminderHour.toIntOrNull()
        val minute = state.reminderMinute.toIntOrNull()

        if (hour == null || hour !in 0..23 || minute == null || minute !in 0..59) {
            _uiState.update {
                it.copy(
                    validationMessages = listOf("提醒時間請輸入有效的 24 小時制時間"),
                    statusMessage = null,
                )
            }
            return
        }

        val settings = ReminderSettings(
            enabled = enabledOverride ?: state.reminderSettings.enabled,
            hour = hour,
            minute = minute,
        )
        reminderSettingsStore.save(settings)
        reminderScheduler.applySettings(settings)

        _uiState.update {
            it.copy(
                reminderSettings = settings,
                reminderHour = settings.hour.toString(),
                reminderMinute = settings.minute.toString(),
                validationMessages = emptyList(),
                statusMessage = if (settings.enabled) {
                    "已設定每日 ${settings.formattedTime} 提醒"
                } else {
                    "已關閉每日提醒"
                },
            )
        }
    }

    private fun TrainingUiState.toDraft(): TrainingRecordDraft =
        when (selectedType) {
            TrainingType.BAND_LEG_CURL -> TrainingRecordDraft(
                dateEpochDay = selectedDateEpochDay,
                type = selectedType,
                durationMinutes = durationMinutes.toIntOrNull(),
                notes = notes,
            )

            TrainingType.LEG_EXTENSION -> TrainingRecordDraft(
                dateEpochDay = selectedDateEpochDay,
                type = selectedType,
                sets = sets.toIntOrNull(),
                reps = reps.toIntOrNull(),
                weightKg = weightKg.toDoubleOrNull(),
                notes = notes,
            )

            TrainingType.RESISTED_CYCLING -> TrainingRecordDraft(
                dateEpochDay = selectedDateEpochDay,
                type = selectedType,
                durationMinutes = durationMinutes.toIntOrNull(),
                resistanceLevel = resistanceLevel.toIntOrNull(),
                notes = notes,
            )
        }
}

class TrainingViewModelFactory(
    private val repository: TrainingRepository,
    private val reminderSettingsStore: ReminderSettingsStore,
    private val reminderScheduler: ReminderScheduler,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrainingViewModel(repository, reminderSettingsStore, reminderScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

