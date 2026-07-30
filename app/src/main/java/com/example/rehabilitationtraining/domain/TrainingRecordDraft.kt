package com.example.rehabilitationtraining.domain

import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType

data class TrainingValidationError(val message: String)

data class TrainingRecordDraft(
    val dateEpochDay: Long,
    val type: TrainingType,
    val recordedTimeMinutes: Int? = null,
    val durationMinutes: Int? = null,
    val sets: Int? = null,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val resistanceLevel: Int? = null,
    val distanceKm: Double? = null,
    val incline: Int? = null,
    val notes: String? = null,
)

fun TrainingRecordDraft.validate(): List<TrainingValidationError> = buildList {
    if (recordedTimeMinutes == null || recordedTimeMinutes !in 0..1439) {
        add(TrainingValidationError("請輸入有效的紀錄時間"))
    }

    when (type) {
        TrainingType.BAND_LEG_CURL -> {
            requirePositive(reps, "請輸入彈力帶彎腿的次數")
            requirePositive(sets, "請輸入彈力帶彎腿的組數")
        }

        TrainingType.LEG_EXTENSION -> {
            requirePositive(sets, "請輸入彈力帶伸腿的組數")
            requirePositive(reps, "請輸入彈力帶伸腿的次數")
            if (weightKg == null) {
                add(TrainingValidationError("請輸入彈力帶伸腿的阻力"))
            } else if (weightKg < 0.0) {
                add(TrainingValidationError("阻力不可小於 0 Kg"))
            }
        }

        TrainingType.RESISTED_CYCLING -> {
            requirePositive(durationMinutes, "請輸入騎器械腳踏車的騎乘時間")
            if (distanceKm == null || distanceKm <= 0.0) {
                add(TrainingValidationError("請輸入大於 0 的騎乘距離"))
            }
            if (resistanceLevel == null) {
                add(TrainingValidationError("請輸入騎器械腳踏車的 LEVEL"))
            } else if (resistanceLevel !in 1..20) {
                add(TrainingValidationError("LEVEL 請輸入 1 到 20"))
            }
        }

        TrainingType.TREADMILL_WALKING -> {
            requirePositive(durationMinutes, "請輸入跑步機走路的走路時間")
            if (distanceKm == null || distanceKm <= 0.0) {
                add(TrainingValidationError("請輸入大於 0 的走路距離"))
            }
            if (incline == null || incline < 0) {
                add(TrainingValidationError("Incline 請輸入 0 以上的整數"))
            }
        }
    }

    if ((notes?.length ?: 0) > 200) {
        add(TrainingValidationError("備註請控制在 200 字以內"))
    }
}

fun TrainingRecordDraft.toEntity(): TrainingRecordEntity {
    val errors = validate()
    require(errors.isEmpty()) {
        errors.joinToString(separator = "；") { it.message }
    }

    return TrainingRecordEntity(
        dateEpochDay = dateEpochDay,
        type = type,
        recordedTimeMinutes = recordedTimeMinutes,
        durationMinutes = when (type) {
            TrainingType.RESISTED_CYCLING,
            TrainingType.TREADMILL_WALKING -> durationMinutes
            TrainingType.BAND_LEG_CURL,
            TrainingType.LEG_EXTENSION -> null
        },
        sets = when (type) {
            TrainingType.BAND_LEG_CURL,
            TrainingType.LEG_EXTENSION -> sets
            TrainingType.RESISTED_CYCLING,
            TrainingType.TREADMILL_WALKING -> null
        },
        reps = when (type) {
            TrainingType.BAND_LEG_CURL,
            TrainingType.LEG_EXTENSION -> reps
            TrainingType.RESISTED_CYCLING,
            TrainingType.TREADMILL_WALKING -> null
        },
        weightKg = if (type == TrainingType.LEG_EXTENSION) weightKg else null,
        resistanceLevel = if (type == TrainingType.RESISTED_CYCLING) resistanceLevel else null,
        distanceKm = if (
            type == TrainingType.RESISTED_CYCLING ||
            type == TrainingType.TREADMILL_WALKING
        ) {
            distanceKm
        } else {
            null
        },
        incline = if (type == TrainingType.TREADMILL_WALKING) incline else null,
        notes = notes?.trim()?.takeIf { it.isNotEmpty() },
    )
}

private fun MutableList<TrainingValidationError>.requirePositive(value: Int?, message: String) {
    if (value == null || value <= 0) {
        add(TrainingValidationError(message))
    }
}
