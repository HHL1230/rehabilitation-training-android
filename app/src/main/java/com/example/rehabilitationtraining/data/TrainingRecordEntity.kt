package com.example.rehabilitationtraining.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "training_records",
    indices = [
        Index(value = ["dateEpochDay"]),
        Index(value = ["type"]),
    ],
)
data class TrainingRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val type: TrainingType,
    val recordedTimeMinutes: Int? = null,
    val durationMinutes: Int? = null,
    val sets: Int? = null,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val resistanceLevel: Int? = null,
    val distanceKm: Double? = null,
    val notes: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
)
