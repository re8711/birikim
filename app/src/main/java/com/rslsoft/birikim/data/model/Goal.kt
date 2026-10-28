package com.rslsoft.birikim.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Long, // kuruş cinsinden
    val savedAmount: Long = 0L, // kuruş cinsinden
    val targetDate: Long, // epoch millis
    val createdAt: Long = System.currentTimeMillis()
)
