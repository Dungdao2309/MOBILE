package com.example.stushare.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents") // 1. Đánh dấu đây là 1 bảng tên là "documents"
data class Document(
    @PrimaryKey // 2. Đánh dấu "id" là khóa chính
    val id: Long,
    val title: String,
    val type: String,
    val imageUrl: String,
    val downloads: Int,
    val rating: Double,
    val author: String,
    val courseCode: String
)