package com.example.stushare.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "requests") // Đánh dấu là bảng
data class DocumentRequest(
    @PrimaryKey // Đánh dấu khóa chính
    val id: String,
    val title: String,
    val authorName: String
)