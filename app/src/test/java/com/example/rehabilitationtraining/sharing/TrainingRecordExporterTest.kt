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
                durationMinutes = 15,
                resistanceLevel = 3,
            ),
        )

        val summary = TrainingRecordExporter.buildSummary(records)

        assertTrue(summary.contains("總紀錄：1 筆"))
        assertTrue(summary.contains("阻力騎腳踏車"))
        assertTrue(summary.contains("15 分鐘"))
    }

    @Test
    fun buildCsvEscapesCommaAndQuoteInNotes() {
        val records = listOf(
            TrainingRecordEntity(
                dateEpochDay = 20_000,
                type = TrainingType.BAND_LEG_CURL,
                durationMinutes = 10,
                notes = "家人說\"慢慢來\",加油",
            ),
        )

        val csv = TrainingRecordExporter.buildCsv(records)

        assertTrue(csv.contains("\"家人說\"\"慢慢來\"\",加油\""))
    }
}

