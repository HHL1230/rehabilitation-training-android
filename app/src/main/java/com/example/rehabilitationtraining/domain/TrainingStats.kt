package com.example.rehabilitationtraining.domain

import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType

data class TrainingTypeStats(
    val recordCount: Int = 0,
    val totalDurationMinutes: Int = 0,
    val totalReps: Int = 0,
    val totalSets: Int = 0,
)

data class TrainingStats(
    val todayRecordCount: Int,
    val last7RecordCount: Int,
    val last30RecordCount: Int,
    val last7DurationMinutes: Int,
    val last30DurationMinutes: Int,
    val last7Reps: Int,
    val last30Reps: Int,
    val last7Sets: Int,
    val last30Sets: Int,
    val byType: Map<TrainingType, TrainingTypeStats>,
) {
    companion object {
        val Empty = TrainingStats(
            todayRecordCount = 0,
            last7RecordCount = 0,
            last30RecordCount = 0,
            last7DurationMinutes = 0,
            last30DurationMinutes = 0,
            last7Reps = 0,
            last30Reps = 0,
            last7Sets = 0,
            last30Sets = 0,
            byType = TrainingType.entries.associateWith { TrainingTypeStats() },
        )

        fun fromRecords(records: List<TrainingRecordEntity>, todayEpochDay: Long): TrainingStats {
            val last7Start = todayEpochDay - 6
            val last30Start = todayEpochDay - 29
            val last7Records = records.filter { it.dateEpochDay in last7Start..todayEpochDay }
            val last30Records = records.filter { it.dateEpochDay in last30Start..todayEpochDay }

            return TrainingStats(
                todayRecordCount = records.count { it.dateEpochDay == todayEpochDay },
                last7RecordCount = last7Records.size,
                last30RecordCount = last30Records.size,
                last7DurationMinutes = last7Records.sumOf { it.durationMinutes ?: 0 },
                last30DurationMinutes = last30Records.sumOf { it.durationMinutes ?: 0 },
                last7Reps = last7Records.sumOf { it.reps ?: 0 },
                last30Reps = last30Records.sumOf { it.reps ?: 0 },
                last7Sets = last7Records.sumOf { it.sets ?: 0 },
                last30Sets = last30Records.sumOf { it.sets ?: 0 },
                byType = TrainingType.entries.associateWith { type ->
                    val typeRecords = records.filter { it.type == type }
                    TrainingTypeStats(
                        recordCount = typeRecords.size,
                        totalDurationMinutes = typeRecords.sumOf { it.durationMinutes ?: 0 },
                        totalReps = typeRecords.sumOf { it.reps ?: 0 },
                        totalSets = typeRecords.sumOf { it.sets ?: 0 },
                    )
                },
            )
        }
    }
}

fun TrainingTypeStats.summaryText(): String = buildList {
    add("$recordCount 筆")
    if (totalDurationMinutes > 0) add("$totalDurationMinutes 分鐘")
    if (totalReps > 0) add("$totalReps 次")
    if (totalSets > 0) add("$totalSets 組")
}.joinToString(separator = "，")

