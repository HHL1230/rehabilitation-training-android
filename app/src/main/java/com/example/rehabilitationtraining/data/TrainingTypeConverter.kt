package com.example.rehabilitationtraining.data

import androidx.room.TypeConverter

class TrainingTypeConverter {
    @TypeConverter
    fun fromTrainingType(type: TrainingType): String = type.name

    @TypeConverter
    fun toTrainingType(value: String): TrainingType = TrainingType.valueOf(value)
}

