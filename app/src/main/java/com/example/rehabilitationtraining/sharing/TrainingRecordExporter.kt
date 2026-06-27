package com.example.rehabilitationtraining.sharing

import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType
import com.example.rehabilitationtraining.domain.TrainingStats
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object TrainingRecordExporter {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    private val numberFormatter = DecimalFormat("0.##", DecimalFormatSymbols(Locale.US))

    fun buildSummary(records: List<TrainingRecordEntity>, title: String = "復健訓練紀錄"): String {
        require(records.isNotEmpty()) { "records must not be empty" }

        val sorted = records.sortedWith(compareBy<TrainingRecordEntity> { it.dateEpochDay }.thenBy { it.createdAtMillis })
        val fromDate = formatDate(sorted.first().dateEpochDay)
        val toDate = formatDate(sorted.last().dateEpochDay)
        val stats = TrainingStats.fromRecords(records, sorted.last().dateEpochDay)
        val totalDuration = records.sumOf { it.durationMinutes ?: 0 }

        return buildString {
            appendLine(title)
            appendLine("期間：$fromDate - $toDate")
            appendLine("總紀錄：${records.size} 筆")
            appendLine("總訓練時間：$totalDuration 分鐘")
            appendLine("近 7 天：${stats.last7RecordCount} 筆，${stats.last7DurationMinutes} 分鐘")
            appendLine()
            appendLine("各項訓練：")
            TrainingType.entries.forEach { type ->
                val typeStats = stats.byType.getValue(type)
                appendLine("- ${type.displayName}：${typeStats.recordCount} 筆，${typeStats.totalDurationMinutes} 分鐘")
            }
            appendLine()
            appendLine("最近紀錄：")
            records.sortedByDescending { it.dateEpochDay }.take(10).forEach { record ->
                appendLine("- ${formatRecord(record)}")
            }
        }.trimEnd()
    }

    fun buildCsv(records: List<TrainingRecordEntity>): String {
        val header = listOf("日期", "訓練項目", "時間(分鐘)", "組數", "次數", "重量(公斤)", "阻力等級", "備註")
        val rows = records
            .sortedWith(compareBy<TrainingRecordEntity> { it.dateEpochDay }.thenBy { it.createdAtMillis })
            .map { record ->
                listOf(
                    formatDate(record.dateEpochDay),
                    record.type.displayName,
                    record.durationMinutes?.toString().orEmpty(),
                    record.sets?.toString().orEmpty(),
                    record.reps?.toString().orEmpty(),
                    record.weightKg?.let { numberFormatter.format(it) }.orEmpty(),
                    record.resistanceLevel?.toString().orEmpty(),
                    record.notes.orEmpty(),
                )
            }

        return (listOf(header) + rows)
            .joinToString(separator = "\n") { row ->
                row.joinToString(separator = ",") { escapeCsv(it) }
            }
    }

    fun formatRecord(record: TrainingRecordEntity): String {
        val detail = when (record.type) {
            TrainingType.BAND_LEG_CURL -> "${record.durationMinutes ?: 0} 分鐘"
            TrainingType.LEG_EXTENSION -> "${record.sets ?: 0} 組 x ${record.reps ?: 0} 次，${record.weightKg?.let { numberFormatter.format(it) } ?: 0} 公斤"
            TrainingType.RESISTED_CYCLING -> "${record.durationMinutes ?: 0} 分鐘，阻力 ${record.resistanceLevel ?: 0}"
        }
        return "${formatDate(record.dateEpochDay)} ${record.type.displayName}：$detail"
    }

    private fun formatDate(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).format(dateFormatter)

    private fun escapeCsv(value: String): String {
        val needsEscaping = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        if (!needsEscaping) return value
        return "\"" + value.replace("\"", "\"\"") + "\""
    }
}

