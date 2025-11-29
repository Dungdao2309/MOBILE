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
    val id: Long,
    val title: String,
    val type: String,
    val imageUrl: String,
    val downloads: Int,
    val rating: Double,
    val author: String,
    val courseCode: String,

    // 👇 THÊM DÒNG NÀY (để lưu ID người đăng)
    val authorId: String? = null
)