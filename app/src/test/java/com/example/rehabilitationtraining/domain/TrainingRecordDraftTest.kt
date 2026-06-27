package com.example.rehabilitationtraining.domain

import com.example.rehabilitationtraining.data.TrainingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingRecordDraftTest {
    @Test
    fun bandLegCurlRequiresDuration() {
        val errors = TrainingRecordDraft(
            dateEpochDay = 20_000,
            type = TrainingType.BAND_LEG_CURL,
            recordedTimeMinutes = 9 * 60,
        ).validate()

        assertTrue(errors.any { it.message.contains("訓練時間") })
    }

    @Test
    fun legExtensionRequiresSetsRepsAndWeight() {
        val errors = TrainingRecordDraft(
            dateEpochDay = 20_000,
            type = TrainingType.LEG_EXTENSION,
            recordedTimeMinutes = 9 * 60,
        ).validate()

        assertEquals(3, errors.size)
    }

    @Test
    fun cyclingResistanceLevelMustBeInRange() {
        val errors = TrainingRecordDraft(
            dateEpochDay = 20_000,
            type = TrainingType.RESISTED_CYCLING,
            recordedTimeMinutes = 9 * 60,
            durationMinutes = 15,
            resistanceLevel = 21,
        ).validate()

        assertTrue(errors.any { it.message.contains("1 到 20") })
    }

    @Test
    fun recordTimeMustBeValidMinutesOfDay() {
        val errors = TrainingRecordDraft(
            dateEpochDay = 20_000,
            type = TrainingType.BAND_LEG_CURL,
            recordedTimeMinutes = 24 * 60,
            durationMinutes = 10,
        ).validate()

        assertTrue(errors.any { it.message.contains("紀錄時間") })
    }

    @Test
    fun toEntityKeepsOnlyFieldsForSelectedTrainingType() {
        val entity = TrainingRecordDraft(
            dateEpochDay = 20_000,
            type = TrainingType.LEG_EXTENSION,
            recordedTimeMinutes = 8 * 60 + 30,
            durationMinutes = 99,
            sets = 3,
            reps = 10,
            weightKg = 2.5,
            resistanceLevel = 4,
            notes = "  穩定完成  ",
        ).toEntity()

        assertEquals(null, entity.durationMinutes)
        assertEquals(null, entity.resistanceLevel)
        assertEquals(8 * 60 + 30, entity.recordedTimeMinutes)
        assertEquals(3, entity.sets)
        assertEquals(10, entity.reps)
        assertEquals(2.5, entity.weightKg ?: 0.0, 0.0)
        assertEquals("穩定完成", entity.notes)
    }
}
