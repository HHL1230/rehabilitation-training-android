package com.example.rehabilitationtraining.domain

import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType

data class TrainingTypeStats(
    val recordCount: Int = 0,
    val totalDurationMinutes: Int = 0,
)

data class TrainingStats(
    val todayRecordCount: Int,
    val last7RecordCount: Int,
    val last30RecordCount: Int,
    val last7DurationMinutes: Int,
    val last30DurationMinutes: Int,
    val byType: Map<TrainingType, TrainingTypeStats>,
) {
    companion object {
        val Empty = TrainingStats(
            todayRecordCount = 0,
            last7RecordCount = 0,
            last30RecordCount = 0,
            last7DurationMinutes = 0,
            last30DurationMinutes = 0,
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
                byType = TrainingType.entries.associateWith { type ->
                    val typeRecords = records.filter { it.type == type }
                    TrainingTypeStats(
                        recordCount = typeRecords.size,
                        totalDurationMinutes = typeRecords.sumOf { it.durationMinutes ?: 0 },
                    )
                },
            )
        }
    }
}

