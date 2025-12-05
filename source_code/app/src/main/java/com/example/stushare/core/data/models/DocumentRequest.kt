package com.example.stushare.core.data.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName // 🟢 Bắt buộc import dòng này
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DocumentRequest(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val authorName: String = "",
    val authorId: String = "",
    val authorAvatar: String? = null,
    val subject: String = "",
    val description: String = "",

    // 🟢 SỬA LẠI: Thêm 2 dòng annotation này để sửa lỗi mapping Firestore
    @get:PropertyName("isSolved")
    @set:PropertyName("isSolved")
    var isSolved: Boolean = false,

    @ServerTimestamp
    val createdAt: Date? = null
) {
    constructor() : this("", "", "", "", null, "", "", false, null)
}