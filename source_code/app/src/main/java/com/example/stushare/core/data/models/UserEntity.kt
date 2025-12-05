package com.example.stushare.core.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName // 🟢 Import cái này

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val contributionPoints: Int = 0,
    val uploadedCount: Int = 0,

    // 🟢 MỚI: Trạng thái khóa tài khoản (Mặc định false)
    @get:PropertyName("isLocked")
    @set:PropertyName("isLocked")
    var isLocked: Boolean = false
) {
    // Constructor rỗng bắt buộc cho Firestore
    constructor() : this("", "", "", null, 0, 0, false)
}