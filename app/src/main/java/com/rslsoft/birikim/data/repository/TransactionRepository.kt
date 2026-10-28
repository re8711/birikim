package com.rslsoft.birikim.data.repository

import com.rslsoft.birikim.data.local.dao.TransactionDao
import com.rslsoft.birikim.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAll(): Flow<List<Transaction>> = transactionDao.getAll()

    suspend fun getById(id: Long): Transaction? = transactionDao.getById(id)

    fun getUpcomingPayments(start: Long, end: Long): Flow<List<Transaction>> =
        transactionDao.getUpcomingPayments(start, end)

    suspend fun insert(transaction: Transaction): Long = transactionDao.insert(transaction)

    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)

    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    suspend fun deleteAll() = transactionDao.deleteAll()
}
