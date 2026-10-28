package com.rslsoft.birikim.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Long, // kuruş cinsinden (cents)
    val type: TransactionType,
    val categoryId: Long,
    val recurrence: Recurrence,
    val startDate: Long, // epoch millis
    val reminderDaysBefore: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isPaid: Boolean = false // for marking upcoming payments as paid
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class Recurrence {
    ONCE,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    SEMI_ANNUALLY,
    YEARLY
}
