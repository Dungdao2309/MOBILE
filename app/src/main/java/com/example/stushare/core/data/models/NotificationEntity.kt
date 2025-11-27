package com.example.stushare.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String, // Ví dụ: "SUCCESS", "INFO", "ERROR"
    val isRead: Boolean = false
)