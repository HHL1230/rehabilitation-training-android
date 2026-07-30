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
                reps = 12,
                sets = 3,
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
        assertEquals(20, stats.last7DurationMinutes)
        assertEquals(12, stats.last7Reps)
        assertEquals(20, stats.last30Reps)
        assertEquals(3, stats.last7Sets)
        assertEquals(5, stats.last30Sets)
        assertEquals(12, stats.byType.getValue(TrainingType.BAND_LEG_CURL).totalReps)
        assertEquals(3, stats.byType.getValue(TrainingType.BAND_LEG_CURL).totalSets)
        assertEquals(0, stats.byType.getValue(TrainingType.BAND_LEG_CURL).totalDurationMinutes)
        assertEquals(1, stats.byType.getValue(TrainingType.LEG_EXTENSION).recordCount)
    }

    @Test
    fun typeSummaryTextHidesZeroValues() {
        assertEquals("2 筆，30 分鐘", TrainingTypeStats(recordCount = 2, totalDurationMinutes = 30).summaryText())
        assertEquals(
            "3 筆，45 次，9 組",
            TrainingTypeStats(recordCount = 3, totalReps = 45, totalSets = 9).summaryText(),
        )
        assertEquals("0 筆", TrainingTypeStats().summaryText())
    }
}

