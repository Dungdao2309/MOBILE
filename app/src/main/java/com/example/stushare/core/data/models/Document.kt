package com.example.stushare.core.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index // Import Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    // ⭐️ CẢI TIẾN: Thêm Index cho các cột thường xuyên dùng để lọc (WHERE)
    // Giúp tăng tốc độ query getDocumentsByType
    indices = [
        androidx.room.Index(value = ["type"]),
        androidx.room.Index(value = ["courseCode"]) // Thêm index cho courseCode nếu sau này cần lọc
    ]
)
data class Document(
    @PrimaryKey // Đánh dấu "id" là khóa chính
    val id: Long,
    val title: String,
    val type: String,
    val imageUrl: String,
    val downloads: Int,
    val rating: Double,
    val author: String,
    val courseCode: String
)