package com.rslsoft.birikim.data.local.dao

import androidx.room.*
import com.rslsoft.birikim.data.model.CalculationHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationHistoryDao {

    @Query("SELECT * FROM calculation_history ORDER BY createdAt DESC")
    fun getAll(): Flow<List<CalculationHistory>>

    @Query("SELECT * FROM calculation_history WHERE id = :id")
    suspend fun getById(id: Long): CalculationHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: CalculationHistory): Long

    @Delete
    suspend fun delete(history: CalculationHistory)

    @Query("DELETE FROM calculation_history")
    suspend fun deleteAll()
}
