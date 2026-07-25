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
        assertTrue(summary.contains("阻力騎腳踏車"))
        assertTrue(summary.contains("08:05"))
        assertTrue(summary.contains("15 分鐘，1.5 公里，阻力 3"))
    }

    @Test
    fun buildTextTableShowsRecordsWithoutAttachment() {
        val records = listOf(
            TrainingRecordEntity(
                dateEpochDay = 20_000,
                type = TrainingType.BAND_LEG_CURL,
                recordedTimeMinutes = 19 * 60 + 45,
                durationMinutes = 10,
            ),
        )

        val table = TrainingRecordExporter.buildTextTable(records)

        assertTrue(table.contains("日期時間"))
        assertTrue(table.contains("19:45"))
        assertTrue(table.contains("彈力帶彎腿"))
        assertTrue(table.contains("10 分鐘"))
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
                ),
            ),
        )

        assertTrue(summary.contains("跑步機走路"))
        assertTrue(summary.contains("30 分鐘，2.5 公里"))
    }
}
