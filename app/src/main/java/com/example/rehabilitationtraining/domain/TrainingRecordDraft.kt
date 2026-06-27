package com.example.rehabilitationtraining.domain

import com.example.rehabilitationtraining.data.TrainingRecordEntity
import com.example.rehabilitationtraining.data.TrainingType

data class TrainingValidationError(val message: String)

data class TrainingRecordDraft(
    val dateEpochDay: Long,
    val type: TrainingType,
    val durationMinutes: Int? = null,
    val sets: Int? = null,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val resistanceLevel: Int? = null,
    val notes: String? = null,
)

fun TrainingRecordDraft.validate(): List<TrainingValidationError> = buildList {
    when (type) {
        TrainingType.BAND_LEG_CURL -> {
            requirePositive(durationMinutes, "請輸入彈力帶彎腿的訓練時間")
        }

        TrainingType.LEG_EXTENSION -> {
            requirePositive(sets, "請輸入阻力伸腿的組數")
            requirePositive(reps, "請輸入阻力伸腿的次數")
            if (weightKg == null) {
                add(TrainingValidationError("請輸入阻力伸腿的重量負荷"))
            } else if (weightKg < 0.0) {
                add(TrainingValidationError("重量負荷不可小於 0 公斤"))
            }
        }

        TrainingType.RESISTED_CYCLING -> {
            requirePositive(durationMinutes, "請輸入騎腳踏車的訓練時間")
            if (resistanceLevel == null) {
                add(TrainingValidationError("請輸入騎腳踏車的阻力等級"))
            } else if (resistanceLevel !in 1..20) {
                add(TrainingValidationError("阻力等級請輸入 1 到 20"))
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
        durationMinutes = when (type) {
            TrainingType.BAND_LEG_CURL,
            TrainingType.RESISTED_CYCLING -> durationMinutes
            TrainingType.LEG_EXTENSION -> null
        },
        sets = if (type == TrainingType.LEG_EXTENSION) sets else null,
        reps = if (type == TrainingType.LEG_EXTENSION) reps else null,
        weightKg = if (type == TrainingType.LEG_EXTENSION) weightKg else null,
        resistanceLevel = if (type == TrainingType.RESISTED_CYCLING) resistanceLevel else null,
        notes = notes?.trim()?.takeIf { it.isNotEmpty() },
    )
}

private fun MutableList<TrainingValidationError>.requirePositive(value: Int?, message: String) {
    if (value == null || value <= 0) {
        add(TrainingValidationError(message))
    }
}
