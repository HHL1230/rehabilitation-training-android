package com.example.rehabilitationtraining.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingRecordDao {
    @Query("SELECT * FROM training_records ORDER BY dateEpochDay DESC, createdAtMillis DESC")
    fun observeAll(): Flow<List<TrainingRecordEntity>>

    @Query(
        """
        SELECT * FROM training_records
        WHERE dateEpochDay BETWEEN :fromEpochDay AND :toEpochDay
        ORDER BY dateEpochDay DESC, createdAtMillis DESC
        """,
    )
    fun observeBetween(fromEpochDay: Long, toEpochDay: Long): Flow<List<TrainingRecordEntity>>

    @Insert
    suspend fun insert(record: TrainingRecordEntity): Long

    @Delete
    suspend fun delete(record: TrainingRecordEntity)
}

