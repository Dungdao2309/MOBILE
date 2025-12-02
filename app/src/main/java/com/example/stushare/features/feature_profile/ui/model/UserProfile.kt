package com.example.stushare.features.feature_profile.ui.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String? = null,

    // 🟢 MỚI: Thông tin mở rộng
    val major: String = "Chưa cập nhật",
    val bio: String = ""
)