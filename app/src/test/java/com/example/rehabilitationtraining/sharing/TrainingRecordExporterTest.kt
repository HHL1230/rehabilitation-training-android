package com.example.rehabilitationtraining.sharing

import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType
import org.junit.Assert.assertTrue
import org.junit.Test

class TrainingRecordExporterTest {
    @Test
    fun buildSummaryIncludesReadableTrainingDetails() {
        val records = listOf(
            TrainingRecordEntity(
                dateEpochDay = 20_000,
                type = TrainingType.RESISTED_CYCLING,
                recordedTimeMinutes = 8 * 60 + 5,
                durationMinutes = 15,
                distanceKm = 1.5,
                resistanceLevel = 3,
            ),
        )

        val summary = TrainingRecordExporter.buildSummary(records)

        assertTrue(summary.contains("總紀錄：1 筆"))
        assertTrue(summary.contains("騎器械腳踏車"))
        assertTrue(summary.contains("08:05"))
        assertTrue(summary.contains("15 分鐘，1.5 km，LEVEL 3"))
    }

    @Test
    fun buildTextTableShowsRecordsWithoutAttachment() {
        val records = listOf(
            TrainingRecordEntity(
                dateEpochDay = 20_000,
                type = TrainingType.BAND_LEG_CURL,
                recordedTimeMinutes = 19 * 60 + 45,
                reps = 10,
                sets = 3,
            ),
        )

        val table = TrainingRecordExporter.buildTextTable(records)

        assertTrue(table.contains("日期時間"))
        assertTrue(table.contains("19:45"))
        assertTrue(table.contains("彈力帶彎腿"))
        assertTrue(table.contains("10 次，3 組"))
    }

    @Test
    fun buildTextTableFallsBackToMinutesForLegacyBandLegCurlRecords() {
        val records = listOf(
            TrainingRecordEntity(
                dateEpochDay = 20_000,
                type = TrainingType.BAND_LEG_CURL,
                recordedTimeMinutes = 19 * 60 + 45,
                durationMinutes = 10,
            ),
        )

        val table = TrainingRecordExporter.buildTextTable(records)

        assertTrue(table.contains("10 分鐘"))
    }

    @Test
    fun buildSummaryFormatsBandLegExtensionFieldsInDisplayOrder() {
        val summary = TrainingRecordExporter.buildSummary(
            listOf(
                TrainingRecordEntity(
                    dateEpochDay = 20_000,
                    type = TrainingType.LEG_EXTENSION,
                    recordedTimeMinutes = 9 * 60,
                    sets = 3,
                    reps = 12,
                    weightKg = 2.0,
                ),
            ),
        )

        assertTrue(summary.contains("彈力帶伸腿"))
        assertTrue(summary.contains("12 次，3 組，2 Kg"))
    }

    @Test
    fun buildSummaryIncludesRepAndSetTotalsForBandLegCurl() {
        val summary = TrainingRecordExporter.buildSummary(
            listOf(
                TrainingRecordEntity(
                    dateEpochDay = 20_000,
                    type = TrainingType.BAND_LEG_CURL,
                    recordedTimeMinutes = 9 * 60,
                    reps = 15,
                    sets = 3,
                ),
            ),
        )

        assertTrue(summary.contains("總訓練次數：15 次"))
        assertTrue(summary.contains("總訓練組數：3 組"))
        assertTrue(summary.contains("近 7 天：1 筆，0 分鐘，15 次，3 組"))
        assertTrue(summary.contains("- 彈力帶彎腿：1 筆，15 次，3 組"))
        assertTrue(summary.contains("- 彈力帶伸腿：0 筆"))
    }

    @Test
    fun buildSummaryFormatsTreadmillWalkingDistance() {
        val summary = TrainingRecordExporter.buildSummary(
            listOf(
                TrainingRecordEntity(
                    dateEpochDay = 20_000,
                    type = TrainingType.TREADMILL_WALKING,
                    recordedTimeMinutes = 9 * 60,
                    durationMinutes = 30,
                    distanceKm = 2.5,
                    incline = 0,
                ),
            ),
        )

        assertTrue(summary.contains("跑步機走路"))
        assertTrue(summary.contains("30 分鐘，2.5 km，Incline 0"))
    }
}
