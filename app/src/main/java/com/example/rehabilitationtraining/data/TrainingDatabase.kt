package com.example.rehabilitationtraining.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TrainingRecordEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(TrainingTypeConverter::class)
abstract class TrainingDatabase : RoomDatabase() {
    abstract fun trainingRecordDao(): TrainingRecordDao

    companion object {
        @Volatile
        private var instance: TrainingDatabase? = null

        fun getInstance(context: Context): TrainingDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrainingDatabase::class.java,
                    "training_records.db",
                ).build().also { instance = it }
            }
    }
}

