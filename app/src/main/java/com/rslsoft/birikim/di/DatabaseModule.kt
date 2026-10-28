package com.rslsoft.birikim.di

import android.content.Context
import com.rslsoft.birikim.data.local.BirikimDatabase
import com.rslsoft.birikim.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BirikimDatabase = BirikimDatabase.getInstance(context)

    @Provides
    fun provideTransactionDao(db: BirikimDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCalculationHistoryDao(db: BirikimDatabase): CalculationHistoryDao = db.calculationHistoryDao()

    @Provides
    fun provideGoalDao(db: BirikimDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideCategoryDao(db: BirikimDatabase): CategoryDao = db.categoryDao()
}
