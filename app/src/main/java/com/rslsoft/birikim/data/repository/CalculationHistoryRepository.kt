package com.rslsoft.birikim.data.repository

import com.rslsoft.birikim.data.local.dao.CalculationHistoryDao
import com.rslsoft.birikim.data.model.CalculationHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculationHistoryRepository @Inject constructor(
    private val calculationHistoryDao: CalculationHistoryDao
) {
    fun getAll(): Flow<List<CalculationHistory>> = calculationHistoryDao.getAll()

    suspend fun getById(id: Long): CalculationHistory? = calculationHistoryDao.getById(id)

    suspend fun insert(history: CalculationHistory): Long = calculationHistoryDao.insert(history)

    suspend fun delete(history: CalculationHistory) = calculationHistoryDao.delete(history)

    suspend fun deleteAll() = calculationHistoryDao.deleteAll()
}
