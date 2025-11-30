package com.example.stushare.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "documents",
    indices = [
        Index(value = ["type"]),
        Index(value = ["courseCode"])
    ]
)
data class Document(
    @PrimaryKey
    val id: String,

    val title: String,
    val type: String,
    val imageUrl: String,

    // ⭐️ THÊM MỚI: Trường này chứa link file (PDF/Word) để tải về
    // Mặc định để chuỗi rỗng "" để tránh lỗi null
    val fileUrl: String = "",

    val downloads: Int,
    val rating: Double,
    val author: String,
    val courseCode: String,

    val createdAt: Long = System.currentTimeMillis(),

    val authorId: String? = null
)