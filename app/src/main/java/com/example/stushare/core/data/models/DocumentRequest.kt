package com.example.stushare.core.data.models

// ⭐️ XÓA: import androidx.room.Entity
// ⭐️ XÓA: import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// ⭐️ XÓA: @Entity(tableName = "requests")
data class DocumentRequest(
    // ⭐️ THÊM: @DocumentId sẽ tự động gán ID của tài liệu Firestore vào đây
    @DocumentId
    val id: String = "", // Cung cấp giá trị mặc định
    val title: String = "",
    val authorName: String = "",

    // ⭐️ CẢI TIẾN: Thêm các trường này để có nhiều thông tin hơn
    val subject: String = "",
    val description: String = "",

    @ServerTimestamp // ⭐️ THÊM: Firestore sẽ tự động điền ngày giờ tạo
    val createdAt: Date? = null
) {
    // ⭐️ THÊM: Cần một constructor rỗng để Firestore đọc dữ liệu
    constructor() : this("", "", "", "", "", null)
}