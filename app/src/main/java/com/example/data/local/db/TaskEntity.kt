package com.example.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val type: String, // "ROUTINE" or "ONE_TIME"
    val category: String,
    val colorAccent: Long,
    val priority: Int = 0, // 0: None, 1: Low, 2: Medium, 3: High
    val scheduledTime: String?, // HH:mm format
    val targetDate: Long?, // For ONE_TIME
    val activeDays: String?, // For ROUTINE, e.g., "1,2,3,4,5,6,7"
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
