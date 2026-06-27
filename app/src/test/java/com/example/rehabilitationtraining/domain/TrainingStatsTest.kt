package com.example.rehabilitationtraining.domain

import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingStatsTest {
    @Test
    fun fromRecordsBuildsTodayAndRangeSummaries() {
        val today = 20_000L
        val records = listOf(
            TrainingRecordEntity(
                dateEpochDay = today,
                type = TrainingType.BAND_LEG_CURL,
                durationMinutes = 12,
            ),
            TrainingRecordEntity(
                dateEpochDay = today - 3,
                type = TrainingType.RESISTED_CYCLING,
                durationMinutes = 20,
                resistanceLevel = 3,
            ),
            TrainingRecordEntity(
                dateEpochDay = today - 8,
                type = TrainingType.LEG_EXTENSION,
                sets = 2,
                reps = 8,
                weightKg = 1.0,
            ),
        )

        val stats = TrainingStats.fromRecords(records, today)

        assertEquals(1, stats.todayRecordCount)
        assertEquals(2, stats.last7RecordCount)
        assertEquals(3, stats.last30RecordCount)
        assertEquals(32, stats.last7DurationMinutes)
        assertEquals(1, stats.byType.getValue(TrainingType.LEG_EXTENSION).recordCount)
    }
}

