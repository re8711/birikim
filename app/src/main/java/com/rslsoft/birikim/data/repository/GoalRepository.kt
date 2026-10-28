package com.rslsoft.birikim.data.repository

import com.rslsoft.birikim.data.local.dao.GoalDao
import com.rslsoft.birikim.data.model.Goal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    fun getAll(): Flow<List<Goal>> = goalDao.getAll()

    suspend fun getById(id: Long): Goal? = goalDao.getById(id)

    suspend fun insert(goal: Goal): Long = goalDao.insert(goal)

    suspend fun update(goal: Goal) = goalDao.update(goal)

    suspend fun delete(goal: Goal) = goalDao.delete(goal)
}
