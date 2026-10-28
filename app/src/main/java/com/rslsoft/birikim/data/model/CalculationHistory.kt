package com.rslsoft.birikim.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class CalculationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: CalculationType,
    val inputsJson: String,
    val resultSummary: String,
    val resultsJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val savedAsTransactionId: Long? = null
)

enum class CalculationType {
    SUBSCRIPTION,
    INSTALLMENT,
    COMPOUND_INTEREST,
    NORMAL
}
