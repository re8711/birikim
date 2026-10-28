package com.rslsoft.birikim.data.repository

import com.rslsoft.birikim.data.local.dao.CategoryDao
import com.rslsoft.birikim.data.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAll(): Flow<List<Category>> = categoryDao.getAll()

    suspend fun getById(id: Long): Category? = categoryDao.getById(id)

    suspend fun insert(category: Category): Long = categoryDao.insert(category)

    suspend fun delete(category: Category) = categoryDao.delete(category)
}
