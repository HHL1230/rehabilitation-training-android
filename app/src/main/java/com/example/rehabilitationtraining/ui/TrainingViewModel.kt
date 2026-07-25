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
import java.time.LocalTime

class TrainingViewModel(
    private val repository: TrainingRepository,
    private val reminderSettingsStore: ReminderSettingsStore,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    init {
        val reminderSettingsByType = reminderSettingsStore.getAllSettings()
        _uiState.update {
            it.copy(
                reminderSettingsByType = reminderSettingsByType,
                reminderTimeInputsByType = reminderSettingsByType.mapValues { (_, settings) ->
                    ReminderTimeInput(
                        hour = settings.hour.toString(),
                        minute = settings.minute.toString(),
                    )
                },
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
        _uiState.update {
            it.copy(
                selectedType = type,
                weightKg = if (type == TrainingType.LEG_EXTENSION && it.weightKg.isEmpty()) "2" else it.weightKg,
                validationMessages = emptyList(),
            )
        }
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

    fun updateRecordHour(value: String) {
        updateDigitsField(value.take(2)) { copy(recordHour = it) }
    }

    fun updateRecordMinute(value: String) {
        updateDigitsField(value.take(2)) { copy(recordMinute = it) }
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

    fun updateDistanceKm(value: String) {
        updateTextField(value) { copy(distanceKm = it) }
    }

    fun updateIncline(value: String) {
        updateDigitsField(value) { copy(incline = it) }
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
                    weightKg = "2",
                    resistanceLevel = "1",
                    distanceKm = "",
                    incline = "0",
                    notes = "",
                    recordHour = LocalTime.now().hour.toString(),
                    recordMinute = LocalTime.now().minute.toString(),
                    validationMessages = emptyList(),
                    statusMessage = "已儲存訓練紀錄",
                )
            }
        }
    }

    fun updateReminderHour(type: TrainingType, value: String) {
        updateReminderInput(type, value.take(2)) { input, updatedValue ->
            input.copy(hour = updatedValue)
        }
    }

    fun updateReminderMinute(type: TrainingType, value: String) {
        updateReminderInput(type, value.take(2)) { input, updatedValue ->
            input.copy(minute = updatedValue)
        }
    }

    fun setReminderEnabled(type: TrainingType, enabled: Boolean) {
        saveReminderSettings(type = type, enabledOverride = enabled)
    }

    fun saveReminderTime(type: TrainingType) {
        saveReminderSettings(type = type, enabledOverride = null)
    }

    fun recordsFromLast7Days(): List<com.example.rehabilitationtraining.data.TrainingRecordEntity> {
        val today = LocalDate.now().toEpochDay()
        return _uiState.value.records.filter { it.dateEpochDay in (today - 6)..today }
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

    private fun updateDigitsField(value: String, update: TrainingUiState.(String) -> TrainingUiState) {
        _uiState.update { state ->
            if (value.all { it.isDigit() }) {
                state.update(value).copy(validationMessages = emptyList())
            } else {
                state
            }
        }
    }

    private fun updateReminderInput(
        type: TrainingType,
        value: String,
        update: (ReminderTimeInput, String) -> ReminderTimeInput,
    ) {
        if (!value.all { it.isDigit() }) return

        _uiState.update { state ->
            val currentInput = state.reminderTimeInputsByType[type] ?: ReminderTimeInput()
            state.copy(
                reminderTimeInputsByType = state.reminderTimeInputsByType +
                    (type to update(currentInput, value)),
                validationMessages = emptyList(),
            )
        }
    }

    private fun saveReminderSettings(type: TrainingType, enabledOverride: Boolean?) {
        val state = _uiState.value
        val input = state.reminderTimeInputsByType[type] ?: ReminderTimeInput()
        val hour = input.hour.toIntOrNull()
        val minute = input.minute.toIntOrNull()

        if (hour == null || hour !in 0..23 || minute == null || minute !in 0..59) {
            _uiState.update {
                it.copy(
                    validationMessages = listOf("${type.displayName}提醒時間請輸入有效的 24 小時制時間"),
                    statusMessage = null,
                )
            }
            return
        }

        val settings = ReminderSettings(
            enabled = enabledOverride ?: (state.reminderSettingsByType[type]?.enabled ?: false),
            hour = hour,
            minute = minute,
        )
        reminderSettingsStore.save(type, settings)
        reminderScheduler.applySettings(type, settings)

        _uiState.update {
            it.copy(
                reminderSettingsByType = it.reminderSettingsByType + (type to settings),
                reminderTimeInputsByType = it.reminderTimeInputsByType +
                    (type to ReminderTimeInput(settings.hour.toString(), settings.minute.toString())),
                validationMessages = emptyList(),
                statusMessage = if (settings.enabled) {
                    "已設定${type.displayName}每日 ${settings.formattedTime} 提醒"
                } else {
                    "已關閉${type.displayName}提醒"
                },
            )
        }
    }

    private fun TrainingUiState.toDraft(): TrainingRecordDraft =
        when (selectedType) {
            TrainingType.BAND_LEG_CURL -> TrainingRecordDraft(
                dateEpochDay = selectedDateEpochDay,
                type = selectedType,
                recordedTimeMinutes = recordTimeMinutesOrNull(),
                durationMinutes = durationMinutes.toIntOrNull(),
                notes = notes,
            )

            TrainingType.LEG_EXTENSION -> TrainingRecordDraft(
                dateEpochDay = selectedDateEpochDay,
                type = selectedType,
                recordedTimeMinutes = recordTimeMinutesOrNull(),
                sets = sets.toIntOrNull(),
                reps = reps.toIntOrNull(),
                weightKg = weightKg.toDoubleOrNull(),
                notes = notes,
            )

            TrainingType.RESISTED_CYCLING -> TrainingRecordDraft(
                dateEpochDay = selectedDateEpochDay,
                type = selectedType,
                recordedTimeMinutes = recordTimeMinutesOrNull(),
                durationMinutes = durationMinutes.toIntOrNull(),
                resistanceLevel = resistanceLevel.toIntOrNull(),
                distanceKm = distanceKm.toDoubleOrNull(),
                notes = notes,
            )

            TrainingType.TREADMILL_WALKING -> TrainingRecordDraft(
                dateEpochDay = selectedDateEpochDay,
                type = selectedType,
                recordedTimeMinutes = recordTimeMinutesOrNull(),
                durationMinutes = durationMinutes.toIntOrNull(),
                distanceKm = distanceKm.toDoubleOrNull(),
                incline = incline.toIntOrNull(),
                notes = notes,
            )
        }

    private fun TrainingUiState.recordTimeMinutesOrNull(): Int? {
        val hour = recordHour.toIntOrNull()
        val minute = recordMinute.toIntOrNull()
        if (hour == null || hour !in 0..23 || minute == null || minute !in 0..59) {
            return null
        }
        return hour * 60 + minute
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
