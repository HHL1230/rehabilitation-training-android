package com.example.rehabilitationtraining.sharing

import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType
import com.example.rehabilitationtraining.domain.TrainingStats
import com.example.rehabilitationtraining.domain.summaryText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TrainingRecordExporter {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val numberFormatter = DecimalFormat("0.##", DecimalFormatSymbols(Locale.US))

    fun buildSummary(records: List<TrainingRecordEntity>, title: String = "復健訓練紀錄"): String {
        require(records.isNotEmpty()) { "records must not be empty" }

        val sorted = records.sortedWith(
            compareBy<TrainingRecordEntity> { it.dateEpochDay }
                .thenBy { it.recordedTimeMinutes ?: fallbackRecordedTimeMinutes(it) }
                .thenBy { it.createdAtMillis },
        )
        val fromDate = formatDate(sorted.first().dateEpochDay)
        val toDate = formatDate(sorted.last().dateEpochDay)
        val stats = TrainingStats.fromRecords(records, sorted.last().dateEpochDay)
        val totalDuration = records.sumOf { it.durationMinutes ?: 0 }
        val totalReps = records.sumOf { it.reps ?: 0 }
        val totalSets = records.sumOf { it.sets ?: 0 }

        return buildString {
            appendLine(title)
            appendLine("期間：$fromDate - $toDate")
            appendLine("總紀錄：${records.size} 筆")
            appendLine("總訓練時間：$totalDuration 分鐘")
            if (totalReps > 0) {
                appendLine("總訓練次數：$totalReps 次")
            }
            if (totalSets > 0) {
                appendLine("總訓練組數：$totalSets 組")
            }
            appendLine(
                buildString {
                    append("近 7 天：${stats.last7RecordCount} 筆，${stats.last7DurationMinutes} 分鐘")
                    if (stats.last7Reps > 0) {
                        append("，${stats.last7Reps} 次")
                    }
                    if (stats.last7Sets > 0) {
                        append("，${stats.last7Sets} 組")
                    }
                },
            )
            appendLine()
            appendLine("各項訓練：")
            TrainingType.entries.forEach { type ->
                val typeStats = stats.byType.getValue(type)
                appendLine("- ${type.displayName}：${typeStats.summaryText()}")
            }
            appendLine()
            appendLine("最近紀錄：")
            appendLine(buildTextTable(records))
        }.trimEnd()
    }

    fun buildTextTable(records: List<TrainingRecordEntity>): String {
        require(records.isNotEmpty()) { "records must not be empty" }

        val rows = records.sortedWith(
            compareByDescending<TrainingRecordEntity> { it.dateEpochDay }
                .thenByDescending { it.recordedTimeMinutes ?: fallbackRecordedTimeMinutes(it) }
                .thenByDescending { it.createdAtMillis },
        ).take(10).map { record ->
            listOf(
                "${formatDate(record.dateEpochDay)} ${formatRecordedTime(record)}",
                record.type.displayName,
                formatRecordDetail(record),
            )
        }

        return buildString {
            appendLine("日期時間         | 項目           | 內容")
            appendLine("---------------|----------------|----------------")
            rows.forEach { row ->
                appendLine(
                    row[0].padEnd(15) +
                        " | " + row[1].padEnd(14) +
                        " | " + row[2],
                )
            }
        }.trimEnd()
    }

    fun formatRecord(record: TrainingRecordEntity): String {
        val detail = formatRecordDetail(record)
        return "${formatDate(record.dateEpochDay)} ${formatRecordedTime(record)} ${record.type.displayName}：$detail"
    }

    private fun formatRecordDetail(record: TrainingRecordEntity): String =
        when (record.type) {
            TrainingType.BAND_LEG_CURL -> when {
                record.reps != null -> "${record.reps} 次，${record.sets ?: 0} 組"
                // 舊版紀錄僅有訓練時間，維持原本的分鐘顯示
                record.durationMinutes != null -> "${record.durationMinutes} 分鐘"
                else -> "0 次，0 組"
            }
            TrainingType.LEG_EXTENSION -> "${record.reps ?: 0} 次，${record.sets ?: 0} 組，${record.weightKg?.let { numberFormatter.format(it) } ?: 0} Kg"
            TrainingType.RESISTED_CYCLING -> "${record.durationMinutes ?: 0} 分鐘，${record.distanceKm?.let { numberFormatter.format(it) } ?: 0} km，LEVEL ${record.resistanceLevel ?: 1}"
            TrainingType.TREADMILL_WALKING -> "${record.durationMinutes ?: 0} 分鐘，${record.distanceKm?.let { numberFormatter.format(it) } ?: 0} km，Incline ${record.incline ?: 0}"
        }

    private fun formatDate(epochDay: Long): String =
        LocalDate.ofEpochDay(epochDay).format(dateFormatter)

    private fun formatRecordedTime(record: TrainingRecordEntity): String =
        LocalTime.ofSecondOfDay(((record.recordedTimeMinutes ?: fallbackRecordedTimeMinutes(record)) * 60).toLong())
            .format(timeFormatter)

    private fun fallbackRecordedTimeMinutes(record: TrainingRecordEntity): Int {
        val time = Instant.ofEpochMilli(record.createdAtMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        return time.hour * 60 + time.minute
    }

}
