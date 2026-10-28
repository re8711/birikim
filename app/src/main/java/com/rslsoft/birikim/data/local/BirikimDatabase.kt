package com.rslsoft.birikim.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rslsoft.birikim.data.local.dao.*
import com.rslsoft.birikim.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Transaction::class,
        CalculationHistory::class,
        Goal::class,
        Category::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BirikimDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun calculationHistoryDao(): CalculationHistoryDao
    abstract fun goalDao(): GoalDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: BirikimDatabase? = null

        fun getInstance(context: Context): BirikimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BirikimDatabase::class.java,
                    "birikim_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getInstance(context).categoryDao().insertAll(Category.DEFAULT_CATEGORIES)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
