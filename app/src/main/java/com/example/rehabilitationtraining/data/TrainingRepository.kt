package com.example.rehabilitationtraining.data

import com.example.rehabilitationtraining.domain.TrainingRecordDraft
import com.example.rehabilitationtraining.domain.toEntity
import com.example.rehabilitationtraining.domain.validate
import kotlinx.coroutines.flow.Flow

class TrainingRepository(private val dao: TrainingRecordDao) {
    fun observeAll(): Flow<List<TrainingRecordEntity>> = dao.observeAll()

    fun observeBetween(fromEpochDay: Long, toEpochDay: Long): Flow<List<TrainingRecordEntity>> =
        dao.observeBetween(fromEpochDay, toEpochDay)

    suspend fun addDraft(draft: TrainingRecordDraft): Long {
        val errors = draft.validate()
        require(errors.isEmpty()) {
            errors.joinToString(separator = "；") { it.message }
        }
        return dao.insert(draft.toEntity())
    }

    suspend fun delete(record: TrainingRecordEntity) {
        dao.delete(record)
    }
}

